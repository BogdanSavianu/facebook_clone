package com.utcn.contentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class ModeratorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ModeratorService moderatorService;

    private final String userServiceBaseUrl = "http://user-service:8082";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        moderatorService = new ModeratorService(restTemplate, userServiceBaseUrl);
    }

    @Test
    void isModerator_WhenUserIsModerator_ShouldReturnTrue() {
        String url = userServiceBaseUrl + "/moderator/isModerator/1";
        Mockito.when(restTemplate.getForObject(ArgumentMatchers.eq(url), ArgumentMatchers.eq(Boolean.class))).thenReturn(true);

        boolean result = moderatorService.isModerator(1);

        assertTrue(result);
        Mockito.verify(restTemplate).getForObject(url, Boolean.class);
    }

    @Test
    void isModerator_WhenUserIsNotModerator_ShouldReturnFalse() {
        String url = userServiceBaseUrl + "/moderator/isModerator/2";
        Mockito.when(restTemplate.getForObject(ArgumentMatchers.eq(url), ArgumentMatchers.eq(Boolean.class))).thenReturn(false);

        boolean result = moderatorService.isModerator(2);

        assertFalse(result);
        Mockito.verify(restTemplate).getForObject(url, Boolean.class);
    }

    @Test
    void isModerator_WhenServiceCallFails_ShouldReturnFalse() {
        String url = userServiceBaseUrl + "/moderator/isModerator/1";
        Mockito.when(restTemplate.getForObject(ArgumentMatchers.eq(url), ArgumentMatchers.eq(Boolean.class)))
              .thenThrow(new RuntimeException("Service unavailable"));

        boolean result = moderatorService.isModerator(1);

        assertFalse(result);
        Mockito.verify(restTemplate).getForObject(url, Boolean.class);
    }

    @Test
    void isModerator_WhenServiceReturnsNull_ShouldReturnFalse() {
        String url = userServiceBaseUrl + "/moderator/isModerator/1";
        Mockito.when(restTemplate.getForObject(ArgumentMatchers.eq(url), ArgumentMatchers.eq(Boolean.class))).thenReturn(null);

        boolean result = moderatorService.isModerator(1);

        assertFalse(result);
        Mockito.verify(restTemplate).getForObject(url, Boolean.class);
    }
} 