package com.utcn.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String bio;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column
    private String gender;

    @Column
    private String location;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "profile_picture")
    @Lob
    private byte[] profilePicture;

    @Column(name = "cover_picture")
    @Lob
    private byte[] coverPicture;

    @Column(name = "score", nullable = false, columnDefinition = "FLOAT DEFAULT 0")
    private Float score = 0.0f;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // New fields for moderator functionality
    @Column(name = "is_moderator", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isModerator = false;
    
    @Column(name = "is_banned", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isBanned = false;
    
    @Column(name = "ban_reason")
    private String banReason;
    
    @Column(name = "banned_at")
    private LocalDateTime bannedAt;
    
    @Column(name = "banned_by")
    private Integer bannedById;
} 