package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.dto.TransferRequest;
import edu.ssw590.summitwealthbank.model.Transaction;
import edu.ssw590.summitwealthbank.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/api/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            Transaction transaction = transferService.transfer(request, email);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (SecurityException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/api/transfer/{accountId}")
    public List<Transaction> getTransactions(@PathVariable Long accountId) {
        return transferService.getTransactions(accountId);
    }

    @GetMapping("/api/transactions/recent")
    public List<Transaction> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        String email = authentication.getName();
        return transferService.getRecentTransactionsByEmail(email, limit);
    }
}