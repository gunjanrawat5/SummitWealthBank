package edu.ssw590.summitwealthbank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminStockUpdatePriceRequest {
    private String symbol;
    private BigDecimal newPrice;
}
