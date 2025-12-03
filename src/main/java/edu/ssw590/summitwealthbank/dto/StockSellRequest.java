package edu.ssw590.summitwealthbank.dto;

import lombok.Data;

@Data
public class StockSellRequest {
    private Long accountId;
    private String stockSymbol;
    private Long quantity;
}
