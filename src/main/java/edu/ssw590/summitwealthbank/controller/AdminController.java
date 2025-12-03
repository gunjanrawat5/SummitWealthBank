package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.dto.AdminActionRequest;
import edu.ssw590.summitwealthbank.dto.AdminStockCreateRequest;
import edu.ssw590.summitwealthbank.dto.AdminStockUpdatePriceRequest;
import edu.ssw590.summitwealthbank.dto.StockTransactionResponse;
import edu.ssw590.summitwealthbank.dto.TransactionResponse;
import edu.ssw590.summitwealthbank.model.Stock;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.service.AdminService;
import edu.ssw590.summitwealthbank.service.AdminStockService;
import edu.ssw590.summitwealthbank.service.StockService;
import edu.ssw590.summitwealthbank.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin
public class AdminController {

    private final AdminService adminService;
    private final AdminStockService adminStockService;
    private final TransferService transferService;
    private final StockService stockService;

    @PostMapping("/freeze")
    public void freeze(@RequestBody AdminActionRequest request) {
        adminService.freezeAccount(request);
    }

    @PostMapping("/unfreeze")
    public void unfreeze(@RequestBody AdminActionRequest request) {
        adminService.unfreezeAccount(request);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAccounts() {
        try {
            return ResponseEntity.ok(adminService.getAllAccounts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/transactions/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TransactionResponse> getAllTransactions(@RequestParam(defaultValue = "100") int limit) {
        return transferService.getAllTransactions(limit);
    }

    @GetMapping("/stock-transactions/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StockTransactionResponse> getAllStockTransactions(@RequestParam(defaultValue = "100") int limit) {
        return stockService.getAllStockTransactions(limit);
    }

    // Stock Management Endpoints

    @PostMapping("/stocks/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createStock(@RequestBody AdminStockCreateRequest request) {
        try {
            Stock stock = adminStockService.createStock(request);
            return ResponseEntity.ok(stock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/stocks/update-price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStockPrice(@RequestBody AdminStockUpdatePriceRequest request) {
        try {
            Stock stock = adminStockService.updateStockPrice(request.getSymbol(), request.getNewPrice());
            return ResponseEntity.ok(stock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stocks")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Stock> getAllStocks() {
        return adminStockService.getAllStocks();
    }

    @DeleteMapping("/stocks/{symbol}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStock(@PathVariable String symbol) {
        try {
            adminStockService.deleteStock(symbol);
            return ResponseEntity.ok("Stock deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}