package edu.ssw590.summitwealthbank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionReference;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerShare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal profitLoss;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String notes;

    public enum TransactionType {
        BUY,
        SELL
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        // Calculate total amount if not set
        if (totalAmount == null && pricePerShare != null && quantity != null) {
            totalAmount = pricePerShare.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
