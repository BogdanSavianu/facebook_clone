package com.facebook.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@SpringBootApplication
public class CommentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        restTemplate.setInterceptors(Collections.singletonList(new ServiceJwtInterceptor()));
        
        return restTemplate;
    }
    
    private static class ServiceJwtInterceptor implements ClientHttpRequestInterceptor {
        private static final String FALLBACK_SERVICE_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzZXJ2aWNlLWFjY291bnQiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNTE2MjM5MDIyfQ.eD7JHh7D2L78KHvLDzPQhJdrw2yCqIVUBCBvn6QkmhE";
        
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String token = null;
                
                if (authentication != null && authentication.getCredentials() instanceof String) {
                    token = (String) authentication.getCredentials();
                }
                
                if (token == null) {
                    token = FALLBACK_SERVICE_TOKEN;
                }
                
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            return execution.execute(request, body);
        }
    }
} 