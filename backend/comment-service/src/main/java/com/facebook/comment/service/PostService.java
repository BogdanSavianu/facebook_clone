package com.facebook.comment.service;

import com.facebook.comment.payload.PostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
public class PostService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${post-service.url}")
    private String postServiceUrl;
    
    public PostDTO getPostById(Long postId) {
        String url = postServiceUrl + "/api/posts/public/" + postId;
        return restTemplate.getForObject(url, PostDTO.class);
    }
    
    public void updatePostStatus(Long postId, String status) {
        String url = postServiceUrl + "/api/posts/" + postId + "/status?status=" + status;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = null;

        if (authentication != null && authentication.getCredentials() instanceof String) {
            token = (String) authentication.getCredentials();
        }

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
             headers.set("Authorization", "Bearer " + token);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
    }
} 