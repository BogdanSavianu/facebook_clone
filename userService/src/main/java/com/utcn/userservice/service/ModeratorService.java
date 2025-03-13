package com.utcn.userservice.service;

import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ModeratorService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public User banUser(Integer moderatorId, Integer userId, String reason) {
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new RuntimeException("Moderator not found"));
                
        if (!moderator.getIsModerator()) {
            throw new RuntimeException("User is not a moderator");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to ban not found"));
                
        if (user.getIsModerator()) {
            throw new RuntimeException("Cannot ban another moderator");
        }
        
        user.setIsBanned(true);
        user.setBanReason(reason);
        user.setBannedAt(LocalDateTime.now());
        user.setBannedById(moderatorId);
        
        User bannedUser = userRepository.save(user);
        
        notificationService.sendBanNotification(bannedUser, reason);
        
        return bannedUser;
    }
    
    @Transactional
    public User unbanUser(Integer moderatorId, Integer userId) {
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new RuntimeException("Moderator not found"));
                
        if (!moderator.getIsModerator()) {
            throw new RuntimeException("User is not a moderator");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to unban not found"));
                
        if (!user.getIsBanned()) {
            throw new RuntimeException("User is not banned");
        }
        
        user.setIsBanned(false);
        user.setBanReason(null);
        user.setBannedAt(null);
        user.setBannedById(null);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User promoteToModerator(Integer adminId, Integer userId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        user.setIsModerator(true);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User demoteFromModerator(Integer adminId, Integer userId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (!user.getIsModerator()) {
            throw new RuntimeException("User is not a moderator");
        }
        
        user.setIsModerator(false);
        
        return userRepository.save(user);
    }
    
    public boolean isModerator(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(User::getIsModerator).orElse(false);
    }
    
    public boolean isBanned(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(User::getIsBanned).orElse(false);
    }
} 