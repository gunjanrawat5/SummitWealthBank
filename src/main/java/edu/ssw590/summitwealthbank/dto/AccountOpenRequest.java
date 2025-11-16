package edu.ssw590.summitwealthbank.dto;

import edu.ssw590.summitwealthbank.model.Account;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountOpenRequest {
    private String username;
    private Account.AccountType type;
    private BigDecimal initialDeposit;
}