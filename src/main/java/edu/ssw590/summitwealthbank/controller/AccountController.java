package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.dto.AccountOpenRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/open")
    public Account openAccount(@RequestBody AccountOpenRequest request) {
        return accountService.openAccount(request);
    }

    @GetMapping("/user/{userId}")
    public List<Account> getUserAccounts(@PathVariable Long userId) {
        return accountService.getUserAccounts(userId);
    }

    @GetMapping("/by-username/{username}")
    public List<Account> getAccountsByUsername(@PathVariable String username) {
        return accountService.getAccountsByUsername(username);
    }
}