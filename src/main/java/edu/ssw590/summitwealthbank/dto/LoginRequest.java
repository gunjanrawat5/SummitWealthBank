package edu.ssw590.summitwealthbank.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
