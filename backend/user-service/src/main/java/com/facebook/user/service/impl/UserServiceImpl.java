package com.facebook.user.service.impl;

import com.facebook.user.model.User;
import com.facebook.user.payload.UserDTO;
import com.facebook.user.repository.UserRepository;
import com.facebook.user.service.UserService;
import com.facebook.user.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public UserDTO updateUserScore(Long userId, Double scoreChange) {
        logger.info("Attempting to update score for userId: {} with change: {}", userId, scoreChange);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {} when attempting to update score.", userId);
                    return new RuntimeException("User not found with id: " + userId);
                });
        
        double newScore = user.getUserScore() + scoreChange;
        user.setUserScore(newScore);
        User savedUser = userRepository.save(user);
        logger.info("Successfully updated score for userId: {}. New score: {}", userId, savedUser.getUserScore());

        notifyAuthServiceOfScoreUpdate(userId, newScore);

        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public UserDTO banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (user.getRoles().contains("ROLE_MODERATOR") || user.getRoles().contains("ROLE_ADMIN")) {
             throw new RuntimeException("Cannot ban a moderator or admin.");
        }
        user.setBanned(true);
        User updatedUser = userRepository.save(user);
        
        notificationService.sendBanNotificationEmail(updatedUser);
        notificationService.sendBanNotificationSms(updatedUser);
        notificationService.sendBanNotificationWhatsApp(updatedUser);

        notifyAuthServiceOfBanStatusUpdate(userId, true);
        
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public UserDTO unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setBanned(false);
        User updatedUser = userRepository.save(user);

        notifyAuthServiceOfBanStatusUpdate(userId, false);

        return convertToDTO(updatedUser);
    }

    private void notifyAuthServiceOfBanStatusUpdate(Long userId, boolean isBanned) {
        try {
            String url = authServiceUrl + "/api/internal/users/" + userId + "/ban-status";
            logger.info("Notifying auth-service at {} about ban status update for userId: {}. Banned: {}", url, userId, isBanned);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Boolean> requestBody = new HashMap<>();
            requestBody.put("banned", isBanned);
            
            HttpEntity<Map<String, Boolean>> entity = new HttpEntity<>(requestBody, headers);
            
            restTemplate.postForEntity(url, entity, String.class);
            logger.info("Successfully notified auth-service for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to notify auth-service about ban status for userId {}: {}", userId, e.getMessage(), e);
        }
    }

    private void notifyAuthServiceOfScoreUpdate(Long userId, Double newScore) {
        try {
            String url = authServiceUrl + "/api/auth/internal/users/" + userId + "/score"; // Corrected path
            logger.info("Notifying auth-service at {} about score update for userId: {}. New score: {}", url, userId, newScore);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Double> entity = new HttpEntity<>(newScore, headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            logger.info("Successfully notified auth-service of score update for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to notify auth-service about score update for userId {}: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserDTO addRoleToUser(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Set<String> roles = user.getRoles();
        roles.add(role.toUpperCase());
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserDTO removeRoleFromUser(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Set<String> roles = user.getRoles();
        roles.remove(role.toUpperCase());
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public UserDTO assignModeratorRoleToCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User must be authenticated to perform this action.");
        }

        Object principal = authentication.getPrincipal();
        User currentUser;
        if (principal instanceof User) {
            currentUser = (User) principal;
        } else if (principal instanceof String) {
            currentUser = userRepository.findByUsername((String) principal)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        } else {
            throw new RuntimeException("Unexpected principal type in security context.");
        }

        User userToUpdate = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUser.getId()));

        Set<String> roles = userToUpdate.getRoles();
        roles.add("ROLE_MODERATOR");
        userToUpdate.setRoles(roles);
        User updatedUser = userRepository.save(userToUpdate);
        
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> updatedAuthorities = roles.stream()
            .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        
        Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            updatedUser, authentication.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return convertToDTO(updatedUser);
    }
    
    @Override
    @PreAuthorize("hasRole('ROLE_MODERATOR') or hasRole('ROLE_ADMIN')")
    public Page<UserDTO> searchUsers(String query, Pageable pageable) {
        Page<User> usersPage = userRepository.findByUsernameContainingIgnoreCase(query, pageable);
        return usersPage.map(this::convertToDTO);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserScore(),
                new ArrayList<>(user.getRoles()),
                user.isBanned(),
                user.getPhoneNumber()
        );
    }
} 