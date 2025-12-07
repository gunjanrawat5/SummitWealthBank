package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.AccountOpenRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.AccountRepository;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("USER")
                .status("ACTIVE")
                .build();

        testAccount = Account.builder()
                .id(1L)
                .user(testUser)
                .type(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .frozen(false)
                .accountNumber("1234567890")
                .build();
    }

    @Test
    @DisplayName("Should open account successfully with initial deposit")
    void testOpenAccountWithInitialDeposit() {
        // Arrange
        AccountOpenRequest request = new AccountOpenRequest();
        request.setEmail("test@example.com");
        request.setType(Account.AccountType.SAVINGS);
        request.setInitialDeposit(new BigDecimal("500.00"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = accountService.openAccount(request);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should open account with zero balance when no initial deposit")
    void testOpenAccountWithoutInitialDeposit() {
        // Arrange
        AccountOpenRequest request = new AccountOpenRequest();
        request.setEmail("test@example.com");
        request.setType(Account.AccountType.CHECKING);
        request.setInitialDeposit(null);

        Account expectedAccount = Account.builder()
                .user(testUser)
                .type(Account.AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .frozen(false)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(expectedAccount);

        // Act
        Account result = accountService.openAccount(request);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(argThat(account ->
            account.getBalance().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testOpenAccountUserNotFound() {
        // Arrange
        AccountOpenRequest request = new AccountOpenRequest();
        request.setEmail("nonexistent@example.com");
        request.setType(Account.AccountType.SAVINGS);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.openAccount(request)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get user accounts by user ID")
    void testGetUserAccounts() {
        // Arrange
        Account account2 = Account.builder()
                .id(2L)
                .user(testUser)
                .type(Account.AccountType.CHECKING)
                .balance(new BigDecimal("500.00"))
                .build();

        List<Account> expectedAccounts = Arrays.asList(testAccount, account2);
        when(accountRepository.findByUserId(1L)).thenReturn(expectedAccounts);

        // Act
        List<Account> result = accountService.getUserAccounts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accountRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Should get account by ID")
    void testGetAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        Account result = accountService.getAccount(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1234567890", result.getAccountNumber());
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void testGetAccountNotFound() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.getAccount(999L)
        );

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should add money to savings account successfully")
    void testAddMoneySuccess() {
        // Arrange
        BigDecimal addAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("1200.00");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = accountService.addMoney(1L, addAmount, "test@example.com");

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(argThat(account ->
            account.getBalance().compareTo(expectedBalance) == 0
        ));
    }

    @Test
    @DisplayName("Should throw exception when adding negative amount")
    void testAddMoneyNegativeAmount() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.addMoney(1L, negativeAmount, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("must be greater than zero"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when adding zero amount")
    void testAddMoneyZeroAmount() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.addMoney(1L, zeroAmount, "test@example.com")
        );

        assertTrue(exception.getMessage().contains("must be greater than zero"));
    }

    @Test
    @DisplayName("Should throw exception when user doesn't own account")
    void testAddMoneyUnauthorized() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> accountService.addMoney(1L, new BigDecimal("100.00"), "other@example.com")
        );

        assertTrue(exception.getMessage().contains("don't have permission"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when adding money to non-savings account")
    void testAddMoneyToCheckingAccount() {
        // Arrange
        Account checkingAccount = Account.builder()
                .id(2L)
                .user(testUser)
                .type(Account.AccountType.CHECKING)
                .balance(new BigDecimal("500.00"))
                .frozen(false)
                .build();

        when(accountRepository.findById(2L)).thenReturn(Optional.of(checkingAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.addMoney(2L, new BigDecimal("100.00"), "test@example.com")
        );

        assertTrue(exception.getMessage().contains("Only savings accounts"));
    }

    @Test
    @DisplayName("Should throw exception when adding money to frozen account")
    void testAddMoneyToFrozenAccount() {
        // Arrange
        testAccount.setFrozen(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.addMoney(1L, new BigDecimal("100.00"), "test@example.com")
        );

        assertTrue(exception.getMessage().contains("frozen account"));
    }

    @Test
    @DisplayName("Should get accounts by email")
    void testGetAccountsByEmail() {
        // Arrange
        List<Account> expectedAccounts = Arrays.asList(testAccount);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserId(1L)).thenReturn(expectedAccounts);

        // Act
        List<Account> result = accountService.getAccountsByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should throw exception when getting accounts for non-existent email")
    void testGetAccountsByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.getAccountsByEmail("nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Should get all accounts")
    void testGetAllAccounts() {
        // Arrange
        List<Account> allAccounts = Arrays.asList(testAccount);
        when(accountRepository.findAll()).thenReturn(allAccounts);

        // Act
        List<Account> result = accountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save account")
    void testSaveAccount() {
        // Arrange
        when(accountRepository.save(testAccount)).thenReturn(testAccount);

        // Act
        accountService.saveAccount(testAccount);

        // Assert
        verify(accountRepository, times(1)).save(testAccount);
    }
}
