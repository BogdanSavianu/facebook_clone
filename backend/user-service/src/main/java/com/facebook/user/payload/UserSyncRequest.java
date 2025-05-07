package com.facebook.user.payload;

public class UserSyncRequest {
    private Long id;
    private String username;
    private String email;
    private Double userScore;
    private boolean banned;
    
    public UserSyncRequest() {
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
    
    public boolean isBanned() {
        return banned;
    }
    
    public void setBanned(boolean banned) {
        this.banned = banned;
    }
} 