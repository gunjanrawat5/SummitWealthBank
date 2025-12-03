package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.StockPortfolioResponse;
import edu.ssw590.summitwealthbank.dto.StockTransactionResponse;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.Stock;
import edu.ssw590.summitwealthbank.model.StockPosition;
import edu.ssw590.summitwealthbank.model.StockTransaction;
import edu.ssw590.summitwealthbank.repository.StockPositionRepository;
import edu.ssw590.summitwealthbank.repository.StockRepository;
import edu.ssw590.summitwealthbank.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final StockRepository stockRepository;
    private final StockPositionRepository positionRepository;
    private final StockTransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * BUY STOCK FLOW:
     * 1. Validate account ownership
     * 2. Fetch stock and verify availability
     * 3. Check account balance
     * 4. Update stock availability (decrement)
     * 5. Update/create position with new average cost basis
     * 6. Deduct from account balance
     * 7. Create transaction record
     */
    public StockTransaction buyStock(Long accountId, String stockSymbol, Long quantity, String userEmail) {
        // 1. Validate ownership
        Account account = validateAccountOwnership(accountId, userEmail);

        // 2. Fetch stock
        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + stockSymbol));

        // 3. Validate availability
        if (stock.getAvailableShares() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Not enough shares available. Available: %d, Requested: %d",
                            stock.getAvailableShares(), quantity));
        }

        // 4. Calculate total cost
        BigDecimal totalCost = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        // 5. Validate balance
        if (account.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient funds in account");
        }

        // 6. Update stock availability (CRITICAL: within transaction)
        stock.setAvailableShares(stock.getAvailableShares() - quantity);
        stockRepository.save(stock);

        // 7. Update or create position
        StockPosition position = positionRepository
                .findByAccountIdAndStockSymbol(accountId, stockSymbol)
                .orElse(StockPosition.builder()
                        .accountId(accountId)
                        .stockSymbol(stockSymbol)
                        .totalShares(0L)
                        .averageCostBasis(BigDecimal.ZERO)
                        .build());

        // Calculate new average cost basis
        BigDecimal existingValue = position.getAverageCostBasis()
                .multiply(BigDecimal.valueOf(position.getTotalShares()));
        BigDecimal newValue = totalCost;
        Long newTotalShares = position.getTotalShares() + quantity;

        BigDecimal newAverageCost = existingValue.add(newValue)
                .divide(BigDecimal.valueOf(newTotalShares), 2, RoundingMode.HALF_UP);

        position.setTotalShares(newTotalShares);
        position.setAverageCostBasis(newAverageCost);
        positionRepository.save(position);

        // 8. Deduct from account
        account.setBalance(account.getBalance().subtract(totalCost));
        accountService.saveAccount(account);

        // 9. Create transaction record
        String txnRef = generateStockTransactionReference();
        StockTransaction transaction = StockTransaction.builder()
                .transactionReference(txnRef)
                .accountId(accountId)
                .stockSymbol(stockSymbol)
                .type(StockTransaction.TransactionType.BUY)
                .quantity(quantity)
                .pricePerShare(stock.getCurrentPrice())
                .totalAmount(totalCost)
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    /**
     * SELL STOCK FLOW:
     * 1. Validate account ownership
     * 2. Fetch position and validate sufficient shares
     * 3. Calculate profit/loss using average cost basis
     * 4. Update stock availability (return shares to pool)
     * 5. Update position (or delete if fully sold)
     * 6. Add proceeds to account balance
     * 7. Create transaction record with profit/loss
     */
    public StockTransaction sellStock(Long accountId, String stockSymbol, Long quantity, String userEmail) {
        // 1. Validate ownership
        Account account = validateAccountOwnership(accountId, userEmail);

        // 2. Fetch position
        StockPosition position = positionRepository
                .findByAccountIdAndStockSymbol(accountId, stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("No position found for " + stockSymbol));

        // 3. Validate quantity
        if (position.getTotalShares() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Not enough shares. Owned: %d, Requested: %d",
                            position.getTotalShares(), quantity));
        }

        // 4. Fetch current stock price
        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + stockSymbol));

        // 5. Calculate proceeds and profit/loss
        BigDecimal proceeds = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal costBasis = position.getAverageCostBasis().multiply(BigDecimal.valueOf(quantity));
        BigDecimal profitLoss = proceeds.subtract(costBasis);

        // 6. Return shares to pool
        stock.setAvailableShares(stock.getAvailableShares() + quantity);
        stockRepository.save(stock);

        // 7. Update position
        Long remainingShares = position.getTotalShares() - quantity;
        if (remainingShares == 0) {
            positionRepository.delete(position);
        } else {
            position.setTotalShares(remainingShares);
            positionRepository.save(position);
        }

        // 8. Add proceeds to account
        account.setBalance(account.getBalance().add(proceeds));
        accountService.saveAccount(account);

        // 9. Create transaction record
        String txnRef = generateStockTransactionReference();
        StockTransaction transaction = StockTransaction.builder()
                .transactionReference(txnRef)
                .accountId(accountId)
                .stockSymbol(stockSymbol)
                .type(StockTransaction.TransactionType.SELL)
                .quantity(quantity)
                .pricePerShare(stock.getCurrentPrice())
                .totalAmount(proceeds)
                .profitLoss(profitLoss)
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    /**
     * Get all stock positions for a user across all their accounts
     */
    public List<StockPortfolioResponse> getUserPortfolio(String email) {
        List<Account> accounts = accountService.getAccountsByEmail(email);
        List<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<StockPosition> positions = positionRepository.findByAccountIds(accountIds);

        return positions.stream()
                .map(this::toPortfolioResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all available stocks for trading
     */
    public List<Stock> getAvailableStocks() {
        return stockRepository.findAvailableStocks();
    }

    /**
     * Get stock transaction history for user
     */
    public List<StockTransactionResponse> getUserTransactionHistory(String email, int limit) {
        List<Account> accounts = accountService.getAccountsByEmail(email);
        List<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<StockTransaction> transactions = transactionRepository
                .findRecentByAccountIds(accountIds, PageRequest.of(0, limit));

        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get specific transaction by reference
     */
    public StockTransactionResponse getTransactionByReference(String transactionReference, String email) {
        StockTransaction transaction = transactionRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionReference));

        // Verify user has access to this transaction
        List<Account> userAccounts = accountService.getAccountsByEmail(email);
        boolean hasAccess = userAccounts.stream()
                .anyMatch(acc -> acc.getId().equals(transaction.getAccountId()));

        if (!hasAccess) {
            throw new SecurityException("You do not have permission to view this transaction");
        }

        return toTransactionResponse(transaction);
    }

    /**
     * Admin method to get all stock transactions
     */
    public List<StockTransactionResponse> getAllStockTransactions(int limit) {
        List<StockTransaction> transactions = transactionRepository.findAllRecent(PageRequest.of(0, limit));
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private Account validateAccountOwnership(Long accountId, String email) {
        Account account = accountService.getAccount(accountId);
        List<Account> userAccounts = accountService.getAccountsByEmail(email);

        boolean ownsAccount = userAccounts.stream()
                .anyMatch(acc -> acc.getId().equals(accountId));

        if (!ownsAccount) {
            throw new SecurityException("You do not have permission to trade from this account");
        }

        if (account.isFrozen()) {
            throw new IllegalStateException("Account is frozen. Please contact support.");
        }

        return account;
    }

    private String generateStockTransactionReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "STK-" + datePart + "-" + uniquePart;
    }

    private StockPortfolioResponse toPortfolioResponse(StockPosition position) {
        Stock stock = stockRepository.findBySymbol(position.getStockSymbol())
                .orElseThrow(() -> new IllegalStateException("Stock not found"));

        BigDecimal marketValue = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(position.getTotalShares()));
        BigDecimal costBasis = position.getAverageCostBasis()
                .multiply(BigDecimal.valueOf(position.getTotalShares()));
        BigDecimal profitLoss = marketValue.subtract(costBasis);
        BigDecimal profitLossPercent = costBasis.compareTo(BigDecimal.ZERO) > 0
                ? profitLoss.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return StockPortfolioResponse.builder()
                .stockSymbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .totalShares(position.getTotalShares())
                .averageCostBasis(position.getAverageCostBasis())
                .currentPrice(stock.getCurrentPrice())
                .marketValue(marketValue)
                .profitLoss(profitLoss)
                .profitLossPercent(profitLossPercent)
                .build();
    }

    private StockTransactionResponse toTransactionResponse(StockTransaction transaction) {
        Account account = accountService.getAccount(transaction.getAccountId());
        Stock stock = stockRepository.findBySymbol(transaction.getStockSymbol())
                .orElseThrow(() -> new IllegalStateException("Stock not found"));

        return StockTransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .stockSymbol(transaction.getStockSymbol())
                .companyName(stock.getCompanyName())
                .type(transaction.getType().name())
                .quantity(transaction.getQuantity())
                .pricePerShare(transaction.getPricePerShare())
                .totalAmount(transaction.getTotalAmount())
                .profitLoss(transaction.getProfitLoss())
                .timestamp(transaction.getTimestamp())
                .accountNumber(account.getAccountNumber())
                .build();
    }
}
