package com.facebook.post.service;

import com.facebook.post.payload.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${user-service.url}")
    private String userServiceUrl;
    
    private HttpHeaders createHeadersWithToken() {
        HttpHeaders headers = new HttpHeaders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Authentication object: {}", authentication);
        
        if (authentication != null && authentication.getCredentials() != null) {
            String token = authentication.getCredentials().toString();
            logger.debug("Token found: {}", token.substring(0, Math.min(10, token.length())) + "...");
            headers.set("Authorization", "Bearer " + token);
        } else {
            logger.warn("No authentication credentials found");
        }
        return headers;
    }
    
    public UserDTO getUserById(Long userId) {
        String url = userServiceUrl + "/api/users/public/" + userId;
        logger.debug("Calling User Service: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(createHeadersWithToken());
        return restTemplate.exchange(url, HttpMethod.GET, entity, UserDTO.class).getBody();
    }
    
    public UserDTO getUserByUsername(String username) {
        String url = userServiceUrl + "/api/users/public/username/" + username;
        logger.debug("Calling User Service to get user by username: {}", url);
        HttpEntity<?> entity = new HttpEntity<>(createHeadersWithToken());
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, UserDTO.class).getBody();
        } catch (Exception e) {
            logger.error("Error calling User Service: {}", e.getMessage());
            throw e;
        }
    }
    
    public void updateUserScore(Long userId, Double scoreChange) {
        String url = userServiceUrl + "/api/users/" + userId + "/score?scoreChange=" + scoreChange;
        HttpEntity<?> entity = new HttpEntity<>(createHeadersWithToken());
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }
} 