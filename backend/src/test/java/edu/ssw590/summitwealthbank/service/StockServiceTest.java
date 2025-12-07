package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.StockPortfolioResponse;
import edu.ssw590.summitwealthbank.dto.StockTransactionResponse;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.Stock;
import edu.ssw590.summitwealthbank.model.StockPosition;
import edu.ssw590.summitwealthbank.model.StockTransaction;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.StockPositionRepository;
import edu.ssw590.summitwealthbank.repository.StockRepository;
import edu.ssw590.summitwealthbank.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Unit Tests")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPositionRepository positionRepository;

    @Mock
    private StockTransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private StockService stockService;

    private User testUser;
    private Account testAccount;
    private Stock testStock;
    private StockPosition testPosition;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testAccount = Account.builder()
                .id(1L)
                .user(testUser)
                .type(Account.AccountType.CHECKING)
                .balance(new BigDecimal("10000.00"))
                .frozen(false)
                .accountNumber("1234567890")
                .build();

        testStock = Stock.builder()
                .id(1L)
                .symbol("AAPL")
                .companyName("Apple Inc.")
                .currentPrice(new BigDecimal("150.00"))
                .availableShares(1000L)
                .build();

        testPosition = StockPosition.builder()
                .id(1L)
                .accountId(1L)
                .stockSymbol("AAPL")
                .totalShares(10L)
                .averageCostBasis(new BigDecimal("140.00"))
                .build();
    }

    @Test
    @DisplayName("Should buy stock successfully")
    void testBuyStockSuccess() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(positionRepository.findByAccountIdAndStockSymbol(1L, "AAPL")).thenReturn(Optional.empty());

        StockTransaction savedTransaction = StockTransaction.builder()
                .id(1L)
                .transactionReference("STK-20251206-ABC123")
                .accountId(1L)
                .stockSymbol("AAPL")
                .type(StockTransaction.TransactionType.BUY)
                .quantity(10L)
                .pricePerShare(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1500.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.save(any(StockTransaction.class))).thenReturn(savedTransaction);

        // Act
        StockTransaction result = stockService.buyStock(1L, "AAPL", 10L, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(990L, testStock.getAvailableShares()); // 1000 - 10
        assertEquals(new BigDecimal("8500.00"), testAccount.getBalance()); // 10000 - 1500
        verify(stockRepository, times(1)).save(testStock);
        verify(positionRepository, times(1)).save(any(StockPosition.class));
        verify(accountService, times(1)).saveAccount(testAccount);
        verify(transactionRepository, times(1)).save(any(StockTransaction.class));
    }

    @Test
    @DisplayName("Should buy stock and update existing position")
    void testBuyStockUpdateExistingPosition() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(positionRepository.findByAccountIdAndStockSymbol(1L, "AAPL")).thenReturn(Optional.of(testPosition));
        when(transactionRepository.save(any(StockTransaction.class))).thenReturn(mock(StockTransaction.class));

        // Act
        stockService.buyStock(1L, "AAPL", 5L, "test@example.com");

        // Assert
        verify(positionRepository).save(argThat(position ->
            position.getTotalShares() == 15L // 10 existing + 5 new
        ));
    }

    @Test
    @DisplayName("Should throw exception when buying stock with insufficient funds")
    void testBuyStockInsufficientFunds() {
        // Arrange
        testAccount.setBalance(new BigDecimal("100.00")); // Not enough
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stockService.buyStock(1L, "AAPL", 10L, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when stock not available")
    void testBuyStockNotAvailable() {
        // Arrange
        testStock.setAvailableShares(5L); // Less than requested
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stockService.buyStock(1L, "AAPL", 10L, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Not enough shares available"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when stock not found")
    void testBuyStockNotFound() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(stockRepository.findBySymbol("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stockService.buyStock(1L, "INVALID", 10L, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Stock not found"));
    }

    @Test
    @DisplayName("Should throw exception when user doesn't own account")
    void testBuyStockUnauthorized() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("other@example.com")).thenReturn(Arrays.asList());

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> stockService.buyStock(1L, "AAPL", 10L, "other@example.com")
        );

        assertTrue(exception.getMessage().contains("do not have permission"));
    }

    @Test
    @DisplayName("Should throw exception when account is frozen")
    void testBuyStockFrozenAccount() {
        // Arrange
        testAccount.setFrozen(true);
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> stockService.buyStock(1L, "AAPL", 10L, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Account is frozen"));
    }

    @Test
    @DisplayName("Should sell stock successfully")
    void testSellStockSuccess() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(positionRepository.findByAccountIdAndStockSymbol(1L, "AAPL")).thenReturn(Optional.of(testPosition));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        StockTransaction savedTransaction = StockTransaction.builder()
                .id(1L)
                .transactionReference("STK-20251206-ABC123")
                .accountId(1L)
                .stockSymbol("AAPL")
                .type(StockTransaction.TransactionType.SELL)
                .quantity(5L)
                .pricePerShare(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("750.00"))
                .profitLoss(new BigDecimal("50.00")) // (150 - 140) * 5
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.save(any(StockTransaction.class))).thenReturn(savedTransaction);

        // Act
        StockTransaction result = stockService.sellStock(1L, "AAPL", 5L, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1005L, testStock.getAvailableShares()); // 1000 + 5
        assertEquals(new BigDecimal("10750.00"), testAccount.getBalance()); // 10000 + 750
        verify(stockRepository, times(1)).save(testStock);
        verify(positionRepository, times(1)).save(any(StockPosition.class));
        verify(accountService, times(1)).saveAccount(testAccount);
    }

    @Test
    @DisplayName("Should sell all shares and delete position")
    void testSellAllShares() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(positionRepository.findByAccountIdAndStockSymbol(1L, "AAPL")).thenReturn(Optional.of(testPosition));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(transactionRepository.save(any(StockTransaction.class))).thenReturn(mock(StockTransaction.class));

        // Act
        stockService.sellStock(1L, "AAPL", 10L, "test@example.com"); // Sell all 10 shares

        // Assert
        verify(positionRepository, times(1)).delete(testPosition);
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when position not found")
    void testSellStockNoPosition() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(positionRepository.findByAccountIdAndStockSymbol(1L, "AAPL")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stockService.sellStock(1L, "AAPL", 5L, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("No position found"));
    }

    @Test
    @DisplayName("Should throw exception when trying to sell more shares than owned")
    void testSellStockInsufficientShares() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(positionRepository.findByAccountIdAndStockSymbol(1L, "AAPL")).thenReturn(Optional.of(testPosition));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stockService.sellStock(1L, "AAPL", 20L, "test@example.com") // More than 10 owned
        );

        assertTrue(exception.getMessage().contains("Not enough shares"));
    }

    @Test
    @DisplayName("Should get user portfolio")
    void testGetUserPortfolio() {
        // Arrange
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(positionRepository.findByAccountIds(anyList())).thenReturn(Arrays.asList(testPosition));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        // Act
        List<StockPortfolioResponse> result = stockService.getUserPortfolio("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        StockPortfolioResponse response = result.get(0);
        assertEquals("AAPL", response.getStockSymbol());
        assertEquals("Apple Inc.", response.getCompanyName());
        assertEquals(10L, response.getTotalShares());
    }

    @Test
    @DisplayName("Should get available stocks")
    void testGetAvailableStocks() {
        // Arrange
        List<Stock> availableStocks = Arrays.asList(testStock);
        when(stockRepository.findAvailableStocks()).thenReturn(availableStocks);

        // Act
        List<Stock> result = stockService.getAvailableStocks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
    }

    @Test
    @DisplayName("Should get user transaction history")
    void testGetUserTransactionHistory() {
        // Arrange
        StockTransaction transaction = StockTransaction.builder()
                .id(1L)
                .transactionReference("STK-20251206-ABC123")
                .accountId(1L)
                .stockSymbol("AAPL")
                .type(StockTransaction.TransactionType.BUY)
                .quantity(10L)
                .pricePerShare(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1500.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(transactionRepository.findRecentByAccountIds(anyList(), any(PageRequest.class)))
                .thenReturn(Arrays.asList(transaction));
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        // Act
        List<StockTransactionResponse> result = stockService.getUserTransactionHistory("test@example.com", 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("STK-20251206-ABC123", result.get(0).getTransactionReference());
    }

    @Test
    @DisplayName("Should get transaction by reference")
    void testGetTransactionByReference() {
        // Arrange
        StockTransaction transaction = StockTransaction.builder()
                .id(1L)
                .transactionReference("STK-20251206-ABC123")
                .accountId(1L)
                .stockSymbol("AAPL")
                .type(StockTransaction.TransactionType.BUY)
                .quantity(10L)
                .pricePerShare(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1500.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.findByTransactionReference("STK-20251206-ABC123"))
                .thenReturn(Optional.of(transaction));
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        // Act
        StockTransactionResponse result = stockService.getTransactionByReference("STK-20251206-ABC123", "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("STK-20251206-ABC123", result.getTransactionReference());
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void testGetTransactionByReferenceNotFound() {
        // Arrange
        when(transactionRepository.findByTransactionReference("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stockService.getTransactionByReference("INVALID", "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Transaction not found"));
    }

    @Test
    @DisplayName("Should throw exception when user doesn't have access to transaction")
    void testGetTransactionByReferenceUnauthorized() {
        // Arrange
        StockTransaction transaction = StockTransaction.builder()
                .id(1L)
                .transactionReference("STK-20251206-ABC123")
                .accountId(99L) // Different account
                .stockSymbol("AAPL")
                .type(StockTransaction.TransactionType.BUY)
                .quantity(10L)
                .pricePerShare(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1500.00"))
                .build();

        when(transactionRepository.findByTransactionReference("STK-20251206-ABC123"))
                .thenReturn(Optional.of(transaction));
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(testAccount));

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> stockService.getTransactionByReference("STK-20251206-ABC123", "test@example.com")
        );

        assertTrue(exception.getMessage().contains("do not have permission"));
    }

    @Test
    @DisplayName("Should get all stock transactions (admin)")
    void testGetAllStockTransactions() {
        // Arrange
        StockTransaction transaction = StockTransaction.builder()
                .id(1L)
                .transactionReference("STK-20251206-ABC123")
                .accountId(1L)
                .stockSymbol("AAPL")
                .type(StockTransaction.TransactionType.BUY)
                .quantity(10L)
                .pricePerShare(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("1500.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.findAllRecent(any(PageRequest.class)))
                .thenReturn(Arrays.asList(transaction));
        when(accountService.getAccount(1L)).thenReturn(testAccount);
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));

        // Act
        List<StockTransactionResponse> result = stockService.getAllStockTransactions(10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
