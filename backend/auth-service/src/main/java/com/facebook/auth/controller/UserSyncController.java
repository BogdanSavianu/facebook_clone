package com.facebook.auth.controller;

import com.facebook.auth.payload.UserBanStatusUpdateRequest;
import com.facebook.auth.service.UserSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/users")
public class UserSyncController {

    private static final Logger logger = LoggerFactory.getLogger(UserSyncController.class);

    @Autowired
    private UserSyncService userSyncService;

    @PostMapping("/{userId}/ban-status")
    public ResponseEntity<?> updateUserBanStatus(@PathVariable Long userId, @RequestBody UserBanStatusUpdateRequest request) {
        try {
            logger.info("Received request to update ban status for userId: {} to {}", userId, request.isBanned());
            userSyncService.updateUserBanStatus(userId, request.isBanned());
            return ResponseEntity.ok("User ban status updated successfully in auth-service for userId: " + userId);
        } catch (Exception e) {
            logger.error("Error processing update ban status request for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to update user ban status: " + e.getMessage());
        }
    }
} 