package edu.ssw590.summitwealthbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPortfolioResponse {
    private String stockSymbol;
    private String companyName;
    private Long totalShares;
    private BigDecimal averageCostBasis;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercent;
}
