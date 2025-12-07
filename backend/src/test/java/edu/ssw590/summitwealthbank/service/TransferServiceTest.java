package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.TransactionResponse;
import edu.ssw590.summitwealthbank.dto.TransferRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.Transaction;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.TransactionRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Account fromAccount;
    private Account toAccount;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        fromAccount = Account.builder()
                .id(1L)
                .user(testUser)
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .frozen(false)
                .accountNumber("1234567890")
                .build();

        toAccount = Account.builder()
                .id(2L)
                .user(testUser)
                .type(Account.AccountType.CHECKING)
                .balance(new BigDecimal("500.00"))
                .frozen(false)
                .accountNumber("0987654321")
                .build();

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setDescription("Test transfer");
    }

    @Test
    @DisplayName("Should transfer money successfully between accounts")
    void testTransferSuccess() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .transactionReference("TXN-20251206-ABC123")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .description("Test transfer")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = transferService.transfer(transferRequest, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("900.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), toAccount.getBalance());
        verify(accountService, times(1)).saveAccount(fromAccount);
        verify(accountService, times(1)).saveAccount(toAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when from account is null")
    void testTransferNullFromAccount() {
        // Arrange
        transferRequest.setFromAccountId(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Both source and destination accounts are required"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when to account is null")
    void testTransferNullToAccount() {
        // Arrange
        transferRequest.setToAccountId(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Both source and destination accounts are required"));
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void testTransferZeroAmount() {
        // Arrange
        transferRequest.setAmount(BigDecimal.ZERO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("must be greater than zero"));
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void testTransferNegativeAmount() {
        // Arrange
        transferRequest.setAmount(new BigDecimal("-50.00"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("must be greater than zero"));
    }

    @Test
    @DisplayName("Should throw exception when description is empty")
    void testTransferEmptyDescription() {
        // Arrange
        transferRequest.setDescription("   ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Description is required"));
    }

    @Test
    @DisplayName("Should throw exception when transferring to same account")
    void testTransferToSameAccount() {
        // Arrange
        transferRequest.setToAccountId(1L); // Same as fromAccountId

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Cannot transfer to the same account"));
    }

    @Test
    @DisplayName("Should throw exception when user doesn't own from account")
    void testTransferUnauthorized() {
        // Arrange
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(toAccount));

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("do not have permission"));
        verify(accountService, never()).saveAccount(any());
    }

    @Test
    @DisplayName("Should throw exception when from account is frozen")
    void testTransferFromFrozenAccount() {
        // Arrange
        fromAccount.setFrozen(true);
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Source account is frozen"));
    }

    @Test
    @DisplayName("Should throw exception when to account is frozen")
    void testTransferToFrozenAccount() {
        // Arrange
        toAccount.setFrozen(true);
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Destination account is frozen"));
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void testTransferInsufficientFunds() {
        // Arrange
        transferRequest.setAmount(new BigDecimal("2000.00")); // More than balance
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.transfer(transferRequest, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    @DisplayName("Should get transactions for an account")
    void testGetTransactions() {
        // Arrange
        List<Transaction> expectedTransactions = Arrays.asList(
            Transaction.builder()
                .id(1L)
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .build()
        );

        when(transactionRepository.findByFromAccountIdOrToAccountId(1L, 1L)).thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = transferService.getTransactions(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findByFromAccountIdOrToAccountId(1L, 1L);
    }

    @Test
    @DisplayName("Should get recent transactions by email")
    void testGetRecentTransactionsByEmail() {
        // Arrange
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));

        Transaction transaction = Transaction.builder()
                .id(1L)
                .transactionReference("TXN-20251206-ABC123")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .description("Test")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.findRecentByAccountIds(anyList(), any(PageRequest.class)))
                .thenReturn(Arrays.asList(transaction));
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);

        // Act
        List<TransactionResponse> result = transferService.getRecentTransactionsByEmail("test@example.com", 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN-20251206-ABC123", result.get(0).getTransactionReference());
    }

    @Test
    @DisplayName("Should search transaction by reference")
    void testSearchByReference() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .transactionReference("TXN-20251206-ABC123")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .description("Test")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.findByTransactionReference("TXN-20251206-ABC123"))
                .thenReturn(Optional.of(transaction));
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);

        // Act
        TransactionResponse result = transferService.searchByReference("TXN-20251206-ABC123", "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("TXN-20251206-ABC123", result.getTransactionReference());
    }

    @Test
    @DisplayName("Should throw exception when transaction reference not found")
    void testSearchByReferenceNotFound() {
        // Arrange
        when(transactionRepository.findByTransactionReference("INVALID"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.searchByReference("INVALID", "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Transaction not found"));
    }

    @Test
    @DisplayName("Should throw exception when user doesn't have access to transaction")
    void testSearchByReferenceUnauthorized() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .transactionReference("TXN-20251206-ABC123")
                .fromAccountId(99L) // Different account
                .toAccountId(98L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(transactionRepository.findByTransactionReference("TXN-20251206-ABC123"))
                .thenReturn(Optional.of(transaction));
        when(accountService.getAccountsByEmail("test@example.com")).thenReturn(Arrays.asList(fromAccount, toAccount));

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> transferService.searchByReference("TXN-20251206-ABC123", "test@example.com")
        );

        assertTrue(exception.getMessage().contains("do not have permission"));
    }

    @Test
    @DisplayName("Should get all transactions (admin)")
    void testGetAllTransactions() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .transactionReference("TXN-20251206-ABC123")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .description("Test")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.findAllRecent(any(PageRequest.class)))
                .thenReturn(Arrays.asList(transaction));
        when(accountService.getAccount(1L)).thenReturn(fromAccount);
        when(accountService.getAccount(2L)).thenReturn(toAccount);

        // Act
        List<TransactionResponse> result = transferService.getAllTransactions(10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
