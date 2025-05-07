package com.facebook.reaction.service;

public interface UserService {
    
    Long getUserIdByUsername(String username);
    
    Double updateUserScore(Long userId, Double scoreDelta);

    com.facebook.reaction.payload.internal.UserDTO fetchUserById(Long userId);
} 