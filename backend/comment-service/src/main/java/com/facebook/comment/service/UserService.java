package com.facebook.comment.service;

import com.facebook.comment.payload.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${user-service.url}")
    private String userServiceUrl;
    
    public UserDTO getUserById(Long userId) {
        String url = userServiceUrl + "/api/users/public/" + userId;
        return restTemplate.getForObject(url, UserDTO.class);
    }
    
    public UserDTO getUserByUsername(String username) {
        String url = userServiceUrl + "/api/users/public/username/" + username;
        return restTemplate.getForObject(url, UserDTO.class);
    }
    
    public void updateUserScore(Long userId, Double scoreChange) {
        String url = userServiceUrl + "/api/users/" + userId + "/score?scoreChange=" + scoreChange;
        restTemplate.postForEntity(url, null, Void.class);
    }
} 