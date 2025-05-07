package com.facebook.user.payload;

import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Double userScore;
    private List<String> roles;
    private boolean banned;
    private String phoneNumber;
    
    public UserDTO() {
    }
    
    public UserDTO(Long id, String username, String email, Double userScore, List<String> roles, boolean banned, String phoneNumber) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userScore = userScore;
        this.roles = roles;
        this.banned = banned;
        this.phoneNumber = phoneNumber;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Double getUserScore() {
        return userScore;
    }
    
    public void setUserScore(Double userScore) {
        this.userScore = userScore;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public boolean isBanned() {
        return banned;
    }
    
    public void setBanned(boolean banned) {
        this.banned = banned;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
} 