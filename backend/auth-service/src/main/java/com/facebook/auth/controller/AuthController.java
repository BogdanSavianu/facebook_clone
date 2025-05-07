package com.facebook.auth.controller;

import com.facebook.auth.model.ERole;
import com.facebook.auth.model.Role;
import com.facebook.auth.model.User;
import com.facebook.auth.payload.request.LoginRequest;
import com.facebook.auth.payload.request.SignupRequest;
import com.facebook.auth.payload.response.JwtResponse;
import com.facebook.auth.payload.response.MessageResponse;
import com.facebook.auth.repository.RoleRepository;
import com.facebook.auth.repository.UserRepository;
import com.facebook.auth.security.jwt.JwtUtils;
import com.facebook.auth.security.services.UserDetailsImpl;
import com.facebook.auth.service.UserSyncService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    UserSyncService userSyncService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (!userDetails.isAccountNonLocked()) {
            return ResponseEntity
                    .status(403)
                    .body(new MessageResponse("Your account has been banned."));
        }
        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(), 
                userDetails.getUsername(), 
                userDetails.getEmail(),
                userRepository.findById(userDetails.getId()).get().getUserScore(),
                roles,
                userDetails.isBanned()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setUserScore(0.0);
        user.setBanned(false);

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        
        userSyncService.syncUserToUserService(savedUser);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PostMapping("/ban/{id}")
    public ResponseEntity<?> banUser(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        user.setBanned(true);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User has been banned successfully!"));
    }
    
    @PostMapping("/unban/{id}")
    public ResponseEntity<?> unbanUser(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        user.setBanned(false);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User has been unbanned successfully!"));
    }

    @PostMapping("/sync-all-users")
    public ResponseEntity<?> syncAllUsers() {
        List<User> allUsers = userRepository.findAll();
        int successCount = 0;
        
        for (User user : allUsers) {
            try {
                userSyncService.syncUserToUserService(user);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to sync user {}: {}", user.getUsername(), e.getMessage());
            }
        }
        
        return ResponseEntity.ok(new MessageResponse("Synced " + successCount + " out of " + allUsers.size() + " users"));
    }

    @PutMapping("/internal/users/{userId}/score")
    public ResponseEntity<?> updateUserScore(@PathVariable("userId") Long userId, @RequestBody Double newScore) {
        logger.info("Received request to update score for user ID: {} to {}", userId, newScore);
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.notFound().build();
        }

        user.setUserScore(newScore);
        userRepository.save(user);
        logger.info("Successfully updated score for user ID: {} to {}", userId, newScore);
        return ResponseEntity.ok(new MessageResponse("User score updated successfully."));
    }
} 