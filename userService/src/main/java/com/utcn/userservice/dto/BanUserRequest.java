package com.utcn.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BanUserRequest {
    @NotNull
    private Integer userId;
    
    @NotBlank
    private String reason;
} 