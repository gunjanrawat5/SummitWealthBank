package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.AccountOpenRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account openAccount(AccountOpenRequest request) {
        Account account = Account.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .balance(request.getInitialDeposit())
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
}