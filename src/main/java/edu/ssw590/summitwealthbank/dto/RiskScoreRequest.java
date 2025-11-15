package edu.ssw590.summitwealthbank.dto;

import lombok.Data;

@Data
public class RiskScoreRequest {
    private Long accountId;
    private int riskScore; // 1 to 5
}