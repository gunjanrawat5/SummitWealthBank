package edu.ssw590.summitwealthbank.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private BigDecimal balance;

    private boolean frozen;

    public enum AccountType {
        CHECKING,
        SAVINGS
    }
}