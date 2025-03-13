package com.utcn.authservice.controller;

import com.utcn.authservice.client.UserServiceClient;
import com.utcn.authservice.dto.JwtResponse;
import com.utcn.authservice.dto.LoginRequest;
import com.utcn.authservice.dto.SignupRequest;
import com.utcn.authservice.entity.User;
import com.utcn.authservice.repository.UserRepository;
import com.utcn.authservice.security.JwtTokenProvider;
import com.utcn.authservice.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserServiceClient userServiceClient;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (userServiceClient.isUserBanned(userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body("Your account has been banned. Please contact support for more information.");
        }

        String jwt = tokenProvider.generateToken(authentication);
        
        return ResponseEntity.ok(new JwtResponse(
                jwt, 
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getFirstName(),
                userPrincipal.getLastName()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already taken!");
        }

        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
        user.setDateOfBirth(signupRequest.getDateOfBirth());
        user.setGender(signupRequest.getGender());
        user.setLocation(signupRequest.getLocation());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now());

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{id}")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location).body("User registered successfully");
    }
    
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = tokenProvider.validateToken(token);
        
        if (isValid) {
            Integer userId = tokenProvider.getUserIdFromJWT(token);
            if (userServiceClient.isUserBanned(userId)) {
                return ResponseEntity.ok(false);
            }
        }
        
        return ResponseEntity.ok(isValid);
    }
} 