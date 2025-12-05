package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class OperationalMetricsController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockRepository stockRepository;

    /**
     * Get operational metrics for dashboard
     */
    @GetMapping("/operational")
    public ResponseEntity<Map<String, Object>> getOperationalMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // User metrics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(user -> !"ADMIN".equals(user.getRole()))
                .count();

        // Account metrics
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.findAll().stream()
                .filter(account -> !account.isFrozen())
                .count();
        long frozenAccounts = accountRepository.findAll().stream()
                .filter(account -> account.isFrozen())
                .count();

        // Calculate total balance across all accounts
        BigDecimal totalBalance = accountRepository.findAll().stream()
                .map(account -> account.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Transaction metrics
        long totalTransactions = transactionRepository.count();
        long totalStockTransactions = stockTransactionRepository.count();

        // Stock metrics
        long totalStocks = stockRepository.count();
        long stocksInInventory = stockRepository.findAll().stream()
                .filter(stock -> stock.getAvailableShares() > 0)
                .count();

        // Build response
        metrics.put("users", Map.of(
                "total", totalUsers,
                "active", activeUsers
        ));

        metrics.put("accounts", Map.of(
                "total", totalAccounts,
                "active", activeAccounts,
                "frozen", frozenAccounts,
                "totalBalance", totalBalance
        ));

        metrics.put("transactions", Map.of(
                "accountTransfers", totalTransactions,
                "stockTrades", totalStockTransactions,
                "total", totalTransactions + totalStockTransactions
        ));

        metrics.put("stocks", Map.of(
                "totalStocks", totalStocks,
                "availableStocks", stocksInInventory
        ));

        return ResponseEntity.ok(metrics);
    }

    /**
     * Get system health summary
     */
    @GetMapping("/health-summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Check database connectivity by querying
            userRepository.count();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
        }

        health.put("application", "UP");
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }
}
