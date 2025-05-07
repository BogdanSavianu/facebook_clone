package com.facebook.reaction.service.impl;

import com.facebook.reaction.payload.internal.UserDTO;
import com.facebook.reaction.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${user-service.url}")
    private String userServiceUrl;
    
    @Override
    public Long getUserIdByUsername(String username) {
        String url = userServiceUrl + "/api/users/public/username/" + username;
        try {
            logger.debug("Fetching UserDTO for username: {} from URL: {}", username, url);
            UserDTO userDTO = restTemplate.getForObject(url, UserDTO.class);
            if (userDTO != null && userDTO.getId() != null) {
                logger.debug("Fetched UserDTO with ID: {} for username: {}", userDTO.getId(), username);
                return userDTO.getId();
            }
            logger.warn("UserDTO or UserDTO.id was null for username: {}", username);
            return null;
        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching user ID for username {}: {} : {}", username, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching user ID for username {}: {}", username, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public Double updateUserScore(Long userId, Double scoreDelta) {
        String url = userServiceUrl + "/api/users/" + userId + "/score"; 
        
        String finalUrl = userServiceUrl + "/api/users/" + userId + "/score?scoreChange=" + scoreDelta;

        try {
            logger.debug("Updating score for userId: {} with delta: {} via URL: {}", userId, scoreDelta, finalUrl);
            ResponseEntity<UserDTO> response = restTemplate.postForEntity(finalUrl, null, UserDTO.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                UserDTO updatedUser = response.getBody();
                logger.info("Successfully updated score for userId: {}. New score: {}", userId, updatedUser.getUserScore());
                return updatedUser.getUserScore();
            }
            logger.warn("Score update for userId {} returned status: {} or body was null", userId, response.getStatusCode());
            return null;
        } catch (HttpClientErrorException e) {
            logger.error("Client error updating score for userId {}: {} : {}", userId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error updating score for userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public com.facebook.reaction.payload.internal.UserDTO fetchUserById(Long userId) {
        String url = userServiceUrl + "/api/users/public/" + userId;
        try {
            logger.debug("Fetching UserDTO for userId: {} from URL: {}", userId, url);
            com.facebook.reaction.payload.internal.UserDTO userDTO = restTemplate.getForObject(url, com.facebook.reaction.payload.internal.UserDTO.class);
            if (userDTO != null) {
                logger.debug("Fetched UserDTO for userId: {}. Score: {}", userId, userDTO.getUserScore());
                return userDTO;
            }
            logger.warn("UserDTO was null for userId: {}", userId);
            return null;
        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching user for userId {}: {} : {}", userId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching user for userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
} 