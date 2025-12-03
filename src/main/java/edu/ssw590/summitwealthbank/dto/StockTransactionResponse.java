package edu.ssw590.summitwealthbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {
    private Long id;
    private String transactionReference;
    private String stockSymbol;
    private String companyName;
    private String type;
    private Long quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private BigDecimal profitLoss;
    private LocalDateTime timestamp;
    private String accountNumber;
}
