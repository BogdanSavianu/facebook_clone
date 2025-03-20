package com.utcn.userservice.service;

import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setGender("Male");
        testUser.setLocation("New York");
        testUser.setPhoneNumber("+1234567890");
        testUser.setScore(0.0f);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setIsModerator(false);
        testUser.setIsBanned(false);
    }

    @Test
    void retrieveUsers_ShouldReturnAllUsers() {
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> actualUsers = userService.retrieveUsers();

        assertEquals(expectedUsers, actualUsers);
        verify(userRepository).findAll();
    }

    @Test
    void retrieveUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        User actualUser = userService.retrieveUserById(1);

        assertNotNull(actualUser);
        assertEquals(testUser, actualUser);
        verify(userRepository).findById(1);
    }

    @Test
    void retrieveUserById_WhenUserDoesNotExist_ShouldReturnNull() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        User actualUser = userService.retrieveUserById(999);

        assertNull(actualUser);
        verify(userRepository).findById(999);
    }

    @Test
    void retrieveUserByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        User actualUser = userService.retrieveUserByEmail("john.doe@example.com");

        assertNotNull(actualUser);
        assertEquals(testUser, actualUser);
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void retrieveUserByEmail_WhenUserDoesNotExist_ShouldReturnNull() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        User actualUser = userService.retrieveUserByEmail("nonexistent@example.com");

        assertNull(actualUser);
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void addUser_ShouldSaveAndReturnUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.addUser(testUser);

        assertNotNull(savedUser);
        assertEquals(testUser, savedUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        User updatedUser = testUser;
        updatedUser.setFirstName("Jane");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(userRepository).save(updatedUser);
    }

    @Test
    void deleteUserById_ShouldDeleteUser() {
        doNothing().when(userRepository).deleteById(1);

        String result = userService.deleteUserById(1);

        assertEquals("User deleted successfully", result);
        verify(userRepository).deleteById(1);
    }
} 