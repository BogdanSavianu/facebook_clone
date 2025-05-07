package com.facebook.user.controller;

import com.facebook.user.model.User;
import com.facebook.user.payload.UserDTO;
import com.facebook.user.payload.MessageResponse;
import com.facebook.user.payload.RoleRequest;
import com.facebook.user.repository.UserRepository;
import com.facebook.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUserScore(@PathVariable Long id, @RequestParam Double scoreChange) {
        UserDTO updatedUser = userService.updateUserScore(id, scoreChange);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PostMapping("/{id}/ban")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<UserDTO> banUser(@PathVariable Long id) {
        logger.info("Request to BAN user with id: {}", id);
        UserDTO updatedUser = userService.banUser(id);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/unban")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<UserDTO> unbanUser(@PathVariable Long id) {
        logger.info("Request to UNBAN user with id: {}", id);
        UserDTO updatedUser = userService.unbanUser(id);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/role/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> addRoleToUser(@PathVariable Long id, @RequestBody RoleRequest roleRequest) {
        logger.info("Request to ADD ROLE '{}' to user id: {}", roleRequest.getRole(), id);
        UserDTO updatedUser = userService.addRoleToUser(id, roleRequest.getRole());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/role/remove")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> removeRoleFromUser(@PathVariable Long id, @RequestBody RoleRequest roleRequest) {
        logger.info("Request to REMOVE ROLE '{}' from user id: {}", roleRequest.getRole(), id);
        UserDTO updatedUser = userService.removeRoleFromUser(id, roleRequest.getRole());
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<UserDTO> getPublicUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/public/username/{username}")
    public ResponseEntity<UserDTO> getPublicUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/public/create")
    public ResponseEntity<?> createPublicUser(@RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            user.setId(userDTO.getId());
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail() != null ? userDTO.getEmail() : userDTO.getUsername() + "@example.com");
            user.setUserScore(userDTO.getUserScore() != null ? userDTO.getUserScore() : 0.0);
            if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                 user.getRoles().add("ROLE_USER");
            } else {
                user.setRoles(new java.util.HashSet<>(userDTO.getRoles()));
            }
            user.setBanned(userDTO.isBanned());
            
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("User created successfully"));
        } catch (Exception e) {
            logger.error("Error creating public user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/me/promote-to-moderator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> promoteToModerator() {
        UserDTO updatedUser = userService.assignModeratorRoleToCurrentUser();
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }
} 