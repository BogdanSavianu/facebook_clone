package com.utcn.contentservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Postable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Postable parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Postable> comments = new ArrayList<>();

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column
    private String title;

    @Column
    private String body;

    @Lob
    @Column
    private byte[] media;

    @Column(name = "media_type")
    private String mediaType;

    @Column
    private String status;

    @Column(name = "privacy_setting")
    private String privacySetting;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "check_in")
    private String checkIn;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "postable", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PostableVote> votes = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PostTag> tags = new ArrayList<>();
    
    @Transient
    public int getVoteCount() {
        if (votes == null || votes.isEmpty()) {
            return 0;
        }
        return votes.stream().mapToInt(vote -> vote.getValue()).sum();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 