package com.utcn.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BanResponse {
    private String message;
    private String reason;
} 