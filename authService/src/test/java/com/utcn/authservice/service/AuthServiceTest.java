package com.utcn.authservice.service;

import com.utcn.authservice.client.UserServiceClient;
import com.utcn.authservice.dto.LoginRequest;
import com.utcn.authservice.dto.SignupRequest;
import com.utcn.authservice.entity.User;
import com.utcn.authservice.repository.UserRepository;
import com.utcn.authservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserServiceClient userServiceClient;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordHash("hashedPassword");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setGender("Male");
        testUser.setLocation("Test Location");
        testUser.setPhoneNumber("1234567890");

        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        signupRequest.setGender("Male");
        signupRequest.setLocation("Test Location");
        signupRequest.setPhoneNumber("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
    }

    @Test
    void whenValidSignupRequest_thenUserShouldBeCreated() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        
        User savedUser = userRepository.save(testUser);
        
        assertNotNull(savedUser);
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(testUser.getFirstName(), savedUser.getFirstName());
        assertEquals(testUser.getLastName(), savedUser.getLastName());
    }

    @Test
    void whenEmailExists_thenSignupShouldFail() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);
        
        boolean exists = userRepository.existsByEmail(signupRequest.getEmail());
        
        assertTrue(exists);
    }

    @Test
    void whenValidToken_thenValidationShouldSucceed() {
        String token = "valid.jwt.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT(token)).thenReturn(1);
        when(userServiceClient.isUserBanned(1)).thenReturn(false);
        
        boolean isValid = tokenProvider.validateToken(token);
        
        assertTrue(isValid);
        assertFalse(userServiceClient.isUserBanned(1));
    }

    @Test
    void whenUserIsBanned_thenValidationShouldFail() {
        String token = "valid.jwt.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT(token)).thenReturn(1);
        when(userServiceClient.isUserBanned(1)).thenReturn(true);
        
        boolean isBanned = userServiceClient.isUserBanned(1);
        
        assertTrue(isBanned);
    }
} 