package com.utcn.contentservice.dto;

import lombok.Data;

@Data
public class ModeratorActionRequest {
    private String action;
    private String content;
    private String reason;
} 