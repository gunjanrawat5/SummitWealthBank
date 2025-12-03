package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.AdminActionRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AccountService accountService;
    private final UserRepository userRepository;

    public void freezeAccount(AdminActionRequest request) {
        Account account = accountService.getAccount(request.getAccountId());
        account.setFrozen(true);
        accountService.saveAccount(account);
    }

    public void unfreezeAccount(AdminActionRequest request) {
        Account account = accountService.getAccount(request.getAccountId());
        account.setFrozen(false);
        accountService.saveAccount(account);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }
}