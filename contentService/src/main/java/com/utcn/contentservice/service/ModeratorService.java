package com.utcn.contentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ModeratorService {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    @Autowired
    public ModeratorService(RestTemplate restTemplate, 
                           @Value("${services.user-service.url}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public boolean isModerator(Integer userId) {
        try {
            String url = userServiceBaseUrl + "/moderator/isModerator/" + userId;
            return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
        } catch (Exception e) {
            return false;
        }
    }
} 