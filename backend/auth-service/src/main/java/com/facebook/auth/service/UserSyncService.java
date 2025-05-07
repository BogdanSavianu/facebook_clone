package com.facebook.auth.service;

import com.facebook.auth.model.User;
import com.facebook.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSyncService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceUrl;
    
    public void syncUserToUserService(User user) {
        try {
            String apiUrl = userServiceUrl + "/api/users/sync";
            logger.debug("Syncing user {} to User Service at {}", user.getUsername(), apiUrl);
            
            // Create payload with user data
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("id", user.getId());
            userPayload.put("username", user.getUsername());
            userPayload.put("email", user.getEmail());
            userPayload.put("userScore", user.getUserScore());
            userPayload.put("banned", user.isBanned());
            
            logger.debug("User sync payload: {}", userPayload);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userPayload, headers);
            
            // Make API call
            logger.debug("Sending sync request to User Service");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("User synchronized successfully to User Service: {}", user.getUsername());
                logger.debug("Sync response: {}", response.getBody());
            } else {
                logger.error("Failed to synchronize user to User Service. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error during user synchronization for user {}: {}", user.getUsername(), e.getMessage(), e);
        }
    }

    @Transactional
    public void updateUserBanStatus(Long userId, boolean isBanned) {
        try {
            logger.info("Updating ban status for userId {} to {} in auth-service", userId, isBanned);
            userRepository.updateBanStatus(userId, isBanned);
            // Verify if the user exists and was updated, or handle UserNotFoundException if preferred.
            // For now, we assume the user exists if user-service is making this call.
            logger.info("Successfully updated ban status for userId {} in auth-service", userId);
        } catch (Exception e) {
            logger.error("Error updating ban status for userId {} in auth-service: {}", userId, e.getMessage(), e);
            // Optionally, rethrow or handle more gracefully
            throw new RuntimeException("Failed to update ban status for user " + userId, e);
        }
    }
} 