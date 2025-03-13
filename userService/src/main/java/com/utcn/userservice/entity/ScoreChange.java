package com.utcn.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "score_changes")
@Data
public class ScoreChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "score_change", nullable = false)
    private Float scoreChange;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
} 