package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.dto.AccountOpenRequest;
import edu.ssw590.summitwealthbank.dto.AddMoneyRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<Account> getMyAccounts(Authentication authentication) {
        String email = authentication.getName();
        return accountService.getAccountsByEmail(email);
    }

    @PostMapping("/open")
    public Account openAccount(@RequestBody AccountOpenRequest request, Authentication authentication) {
        String email = authentication.getName();
        request.setEmail(email);
        return accountService.openAccount(request);
    }

    @GetMapping("/user/{userId}")
    public List<Account> getUserAccounts(@PathVariable Long userId) {
        return accountService.getUserAccounts(userId);
    }

    @GetMapping("/by-email/{email}")
    public List<Account> getAccountsByEmail(@PathVariable String email) {
        return accountService.getAccountsByEmail(email);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> addMoney(@RequestBody AddMoneyRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            Account account = accountService.addMoney(request.getAccountId(), request.getAmount(), email);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}