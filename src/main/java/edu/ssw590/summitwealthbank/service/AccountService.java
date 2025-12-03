package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.AccountOpenRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.AccountRepository;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account openAccount(AccountOpenRequest request) {
        User user = userRepository.findByEmail(request.getEmail())  // CHANGED from getUsername
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getEmail()));

        Account account = Account.builder()
                .user(user)
                .type(request.getType())
                .balance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO)
                .frozen(false)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }

    public List<Account> getAccountsByEmail(String email) {  // CHANGED method name
        User user = userRepository.findByEmail(email)        // CHANGED from findByUsername
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        return accountRepository.findByUserId(user.getId());
    }

    @Transactional
    public Account addMoney(Long accountId, BigDecimal amount, String userEmail) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Get account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Validate ownership
        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("You don't have permission to access this account");
        }

        // Validate account type - only SAVINGS accounts can receive deposits
        if (account.getType() != Account.AccountType.SAVINGS) {
            throw new IllegalArgumentException("Only savings accounts can receive deposits. Account type: " + account.getType());
        }

        // Validate account is not frozen
        if (account.isFrozen()) {
            throw new IllegalArgumentException("Cannot add money to a frozen account");
        }

        // Add money
        account.setBalance(account.getBalance().add(amount));

        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}