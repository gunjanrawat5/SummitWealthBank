package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.AccountOpenRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.AccountRepository;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account openAccount(AccountOpenRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUsername()));

        Account account = Account.builder()
                .user(user) // set full user object
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



    public List<Account> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return accountRepository.findByUserId(user.getId());
    }




}