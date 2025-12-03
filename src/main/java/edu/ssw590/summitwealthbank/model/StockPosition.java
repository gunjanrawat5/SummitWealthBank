package edu.ssw590.summitwealthbank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_position",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "stock_symbol"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(nullable = false)
    private Long totalShares;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal averageCostBasis;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to calculate current market value
    @Transient
    public BigDecimal getMarketValue(BigDecimal currentPrice) {
        return currentPrice.multiply(BigDecimal.valueOf(totalShares));
    }

    // Helper method to calculate total profit/loss
    @Transient
    public BigDecimal getProfitLoss(BigDecimal currentPrice) {
        BigDecimal costBasis = averageCostBasis.multiply(BigDecimal.valueOf(totalShares));
        BigDecimal marketValue = getMarketValue(currentPrice);
        return marketValue.subtract(costBasis);
    }
}
