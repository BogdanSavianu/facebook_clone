package com.facebook.user.controller;

import com.facebook.user.model.User;
import com.facebook.user.payload.UserSyncRequest;
import com.facebook.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSyncController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@RequestBody UserSyncRequest syncRequest) {
        logger.info("Received sync request for user: {}", syncRequest.getUsername());
        
        try {
            Optional<User> existingUser = userRepository.findByUsername(syncRequest.getUsername());
            
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setEmail(syncRequest.getEmail());
                user.setUserScore(syncRequest.getUserScore());
                user.setBanned(syncRequest.isBanned());
            } else {
                user = new User();
                if (syncRequest.getId() != null) {
                    user.setId(syncRequest.getId());
                }
                user.setUsername(syncRequest.getUsername());
                user.setEmail(syncRequest.getEmail());
                user.setUserScore(syncRequest.getUserScore());
                user.setBanned(syncRequest.isBanned());
            }
            
            userRepository.save(user);
            
            logger.info("User synchronized successfully: {}", syncRequest.getUsername());
            return ResponseEntity.ok().body("User synchronized successfully");
        } catch (Exception e) {
            logger.error("Error synchronizing user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error synchronizing user: " + e.getMessage());
        }
    }
} 