package com.utcn.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utcn.authservice.dto.LoginRequest;
import com.utcn.authservice.dto.SignupRequest;
import com.utcn.authservice.entity.User;
import com.utcn.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.beans.factory.annotation.Value;
import com.utcn.authservice.security.JwtTokenProvider;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");
        signupRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        signupRequest.setGender("Male");
        signupRequest.setLocation("Test Location");
        signupRequest.setPhoneNumber("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void whenValidSignupRequest_thenReturn201() throws Exception {
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void whenDuplicateEmail_thenReturn400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // Second signup with same email
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenValidLogin_thenReturnJwtToken() throws Exception {
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        ResultActions result = mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(loginRequest.getEmail()));
    }

    @Test
    void whenInvalidLogin_thenReturn401() throws Exception {
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenValidateValidToken_thenReturnTrue() throws Exception {
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        ResultActions loginResult = mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        String response = loginResult.andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/auth/validate")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
} 