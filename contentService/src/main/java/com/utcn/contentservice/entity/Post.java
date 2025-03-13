package com.utcn.contentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PostComment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PostTag> tags = new HashSet<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<PostVote> votes = new HashSet<>();
    
    @Transient
    public int getVoteCount() {
        if (votes == null || votes.isEmpty()) {
            return 0;
        }
        return votes.stream().mapToInt(PostVote::getValue).sum();
    }
} 