package edu.ssw590.summitwealthbank.dto;

import edu.ssw590.summitwealthbank.model.Account;
import lombok.Data;

@Data
public class AccountOpenRequest {
    private Long userId;
    private Account.AccountType type;
}