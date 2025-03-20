package com.utcn.userservice.controller;

import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    void whenGetAllUsers_thenReturnUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void whenGetUserById_thenReturnUser() throws Exception {
        mockMvc.perform(get("/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void whenGetUserByEmail_thenReturnUser() throws Exception {
        mockMvc.perform(get("/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void whenCreateUser_thenCreateUser() throws Exception {
        User newUser = new User();
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setPasswordHash("hashedPassword");
        newUser.setDateOfBirth(LocalDate.of(1992, 1, 1));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane.smith@example.com\",\"passwordHash\":\"hashedPassword\",\"dateOfBirth\":\"1992-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    void whenUpdateUser_thenUpdateUser() throws Exception {
        testUser.setFirstName("Johnny");
        testUser.setLastName("Doe Jr.");

        mockMvc.perform(put("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Johnny\",\"lastName\":\"Doe Jr.\",\"email\":\"john.doe@example.com\",\"passwordHash\":\"hashedPassword\",\"dateOfBirth\":\"1990-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Doe Jr."));
    }

    @Test
    void whenDeleteUser_thenDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/" + testUser.getId()))
                .andExpect(status().isOk());

        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        assertTrue(users.isEmpty());
    }

    @Test
    void whenGetNonExistentUserById_thenReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetNonExistentUserByEmail_thenReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpdateNonExistentUser_thenReturnNotFound() throws Exception {
        mockMvc.perform(put("/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Johnny\",\"lastName\":\"Doe Jr.\",\"email\":\"john.doe@example.com\",\"passwordHash\":\"hashedPassword\",\"dateOfBirth\":\"1990-01-01\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteNonExistentUser_thenReturnNotFound() throws Exception {
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }
} 