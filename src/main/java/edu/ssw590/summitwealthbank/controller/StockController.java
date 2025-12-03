package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.dto.*;
import edu.ssw590.summitwealthbank.model.Stock;
import edu.ssw590.summitwealthbank.model.StockTransaction;
import edu.ssw590.summitwealthbank.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin
public class StockController {

    private final StockService stockService;

    @GetMapping("/available")
    public List<Stock> getAvailableStocks() {
        return stockService.getAvailableStocks();
    }

    @GetMapping("/portfolio")
    public List<StockPortfolioResponse> getMyPortfolio(Authentication authentication) {
        String email = authentication.getName();
        return stockService.getUserPortfolio(email);
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(
            @RequestBody StockBuyRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            StockTransaction transaction = stockService.buyStock(
                    request.getAccountId(),
                    request.getStockSymbol(),
                    request.getQuantity(),
                    email
            );
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(
            @RequestBody StockSellRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            StockTransaction transaction = stockService.sellStock(
                    request.getAccountId(),
                    request.getStockSymbol(),
                    request.getQuantity(),
                    email
            );
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/transactions")
    public List<StockTransactionResponse> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit) {
        String email = authentication.getName();
        return stockService.getUserTransactionHistory(email, limit);
    }

    @GetMapping("/transactions/{reference}")
    public ResponseEntity<?> getTransactionByReference(
            @PathVariable String reference,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            StockTransactionResponse transaction = stockService.getTransactionByReference(reference, email);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
