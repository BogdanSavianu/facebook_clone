package com.facebook.auth.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Double userScore;
    private List<String> roles;
    private boolean banned;

    public JwtResponse(String token, Long id, String username, String email, Double userScore, List<String> roles, boolean banned) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.userScore = userScore;
        this.roles = roles;
        this.banned = banned;
    }
} 