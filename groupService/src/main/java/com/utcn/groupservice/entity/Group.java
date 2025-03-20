package com.utcn.groupservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(name = "profile_picture")
    private byte[] profilePicture;

    @Lob
    @Column(name = "cover_picture")
    private byte[] coverPicture;

    @Column
    private String description;

    @Column(name = "privacy_setting", nullable = false)
    private String privacySetting;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GroupMember> members = new HashSet<>();
} 