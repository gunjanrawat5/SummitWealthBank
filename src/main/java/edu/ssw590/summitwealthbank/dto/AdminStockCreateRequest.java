package edu.ssw590.summitwealthbank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminStockCreateRequest {
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private Long totalShares;
    private String sector;
    private String description;
}
