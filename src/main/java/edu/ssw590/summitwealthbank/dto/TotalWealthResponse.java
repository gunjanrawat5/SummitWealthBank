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
public class TotalWealthResponse {
    private BigDecimal checkingBalance;
    private BigDecimal savingsBalance;
    private BigDecimal stockPortfolioValue;
    private BigDecimal totalWealth;
}
