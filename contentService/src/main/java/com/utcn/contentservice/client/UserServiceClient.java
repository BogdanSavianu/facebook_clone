package com.utcn.contentservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceClient {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    public float adjustUserScore(Integer userId, float adjustment, String reason, Integer referenceId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(userServiceUrl)
                .path("/api/users/scores/{userId}/adjust")
                .buildAndExpand(userId)
                .toUriString();
        
        Map<String, Object> params = new HashMap<>();
        params.put("adjustment", adjustment);
        params.put("reason", reason);
        params.put("referenceId", referenceId);
        
        ResponseEntity<Float> response = restTemplate.postForEntity(
                url + "?adjustment={adjustment}&reason={reason}&referenceId={referenceId}", 
                null, 
                Float.class, 
                params);
        
        return response.getBody();
    }
} 