package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.AdminStockCreateRequest;
import edu.ssw590.summitwealthbank.model.Stock;
import edu.ssw590.summitwealthbank.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStockService {

    private final StockRepository stockRepository;

    @Transactional
    public Stock createStock(AdminStockCreateRequest request) {
        // Validate symbol doesn't exist
        if (stockRepository.findBySymbol(request.getSymbol()).isPresent()) {
            throw new IllegalArgumentException("Stock with symbol " + request.getSymbol() + " already exists");
        }

        Stock stock = Stock.builder()
                .symbol(request.getSymbol().toUpperCase())
                .companyName(request.getCompanyName())
                .currentPrice(request.getCurrentPrice())
                .totalShares(request.getTotalShares())
                .availableShares(request.getTotalShares()) // Initially all shares available
                .sector(request.getSector())
                .description(request.getDescription())
                .build();

        return stockRepository.save(stock);
    }

    @Transactional
    public Stock updateStockPrice(String symbol, BigDecimal newPrice) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + symbol));

        stock.setCurrentPrice(newPrice);
        return stockRepository.save(stock);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAllByOrderByCompanyNameAsc();
    }

    @Transactional
    public void deleteStock(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + symbol));

        // Only allow deletion if no shares are owned by users
        if (stock.getAvailableShares() < stock.getTotalShares()) {
            throw new IllegalStateException("Cannot delete stock with active positions");
        }

        stockRepository.delete(stock);
    }
}
