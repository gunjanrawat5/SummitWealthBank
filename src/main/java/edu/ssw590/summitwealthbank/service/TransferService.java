package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.TransactionResponse;
import edu.ssw590.summitwealthbank.dto.TransferRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.Transaction;
import edu.ssw590.summitwealthbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public Transaction transfer(TransferRequest request, String email) {
        // Validate request
        if (request.getFromAccountId() == null || request.getToAccountId() == null) {
            throw new IllegalArgumentException("Both source and destination accounts are required");
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required and cannot be blank");
        }

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account from = accountService.getAccount(request.getFromAccountId());
        Account to = accountService.getAccount(request.getToAccountId());

        // Verify ownership - user must own the source account
        List<Account> userAccounts = accountService.getAccountsByEmail(email);
        boolean ownsFromAccount = userAccounts.stream()
                .anyMatch(acc -> acc.getId().equals(from.getId()));

        if (!ownsFromAccount) {
            throw new SecurityException("You do not have permission to transfer from this account");
        }

        if (from.isFrozen()) {
            throw new IllegalStateException("Source account is frozen. Please contact support.");
        }

        if (to.isFrozen()) {
            throw new IllegalStateException("Destination account is frozen. Transfer cannot be completed.");
        }

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));

        accountService.saveAccount(from);
        accountService.saveAccount(to);

        // Generate unique transaction reference
        String transactionReference = generateTransactionReference();

        Transaction tx = Transaction.builder()
                .transactionReference(transactionReference)
                .fromAccountId(from.getId())
                .toAccountId(to.getId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepository.save(tx);
    }

    private String generateTransactionReference() {
        // Format: TXN-YYYYMMDD-XXXXXX (e.g., TXN-20251202-A3F9B2)
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TXN-" + datePart + "-" + uniquePart;
    }

    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }

    public List<TransactionResponse> getRecentTransactionsByEmail(String email, int limit) {
        List<Account> accounts = accountService.getAccountsByEmail(email);

        if (accounts.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<Transaction> transactions = transactionRepository.findRecentByAccountIds(accountIds, PageRequest.of(0, limit));

        // Convert to TransactionResponse with account numbers
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        // Fetch account details to get account numbers
        Account fromAccount = accountService.getAccount(transaction.getFromAccountId());
        Account toAccount = accountService.getAccount(transaction.getToAccountId());

        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .fromAccountId(transaction.getFromAccountId())
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccountId(transaction.getToAccountId())
                .toAccountNumber(toAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    public TransactionResponse searchByReference(String transactionReference, String email) {
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with reference: " + transactionReference));

        // Verify user has access to this transaction
        List<Account> userAccounts = accountService.getAccountsByEmail(email);
        List<Long> userAccountIds = userAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        boolean hasAccess = userAccountIds.contains(transaction.getFromAccountId())
                || userAccountIds.contains(transaction.getToAccountId());

        if (!hasAccess) {
            throw new SecurityException("You do not have permission to view this transaction");
        }

        return toTransactionResponse(transaction);
    }

    // Admin method to get all transactions
    public List<TransactionResponse> getAllTransactions(int limit) {
        List<Transaction> transactions = transactionRepository.findAllRecent(PageRequest.of(0, limit));
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }
}