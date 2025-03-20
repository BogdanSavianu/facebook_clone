package com.utcn.messagingservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "conversation_name")
    private String conversationName;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private Set<ConversationMember> members = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<ConversationMessage> messages = new HashSet<>();
} 