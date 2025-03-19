package com.utcn.contentservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public float adjustUserScore(Integer userId, float adjustment, String reason, Integer referenceId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl.trim())
                    .path("/api/users/scores/{userId}/adjust")
                    .queryParam("adjustment", "{adjustment}")
                    .queryParam("reason", "{reason}")
                    .queryParam("referenceId", "{referenceId}")
                    .encode()
                    .buildAndExpand(userId, adjustment, reason, referenceId)
                    .toUriString();
            
            ResponseEntity<Float> response = restTemplate.postForEntity(
                    url, 
                    null, 
                    Float.class);
            
            return response.getBody() != null ? response.getBody() : 0.0f;
        } catch (ResourceAccessException e) {
            // This is expected in test environment where user service is not running
            logger.debug("User service is not available (this is expected in test environment): {}", e.getMessage());
            return 0.0f;
        } catch (Exception e) {
            logger.error("Failed to adjust user score: {}", e.getMessage());
            return 0.0f;
        }
    }
} 