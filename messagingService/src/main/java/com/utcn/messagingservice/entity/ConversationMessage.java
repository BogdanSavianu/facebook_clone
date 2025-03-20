package com.utcn.messagingservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "conversation_messages")
public class ConversationMessage {

    @EmbeddedId
    private ConversationMessageId id;

    @Column(name = "sender_id", insertable = false, updatable = false)
    private Integer senderId;

    @ManyToOne
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    @JsonBackReference
    private Conversation conversation;

    @Column(nullable = false)
    private String content;

    @Lob
    @Column
    private byte[] media;

    @Column(name = "media_type")
    private String mediaType;

    @Column(nullable = false)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Getter
    @Setter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConversationMessageId implements Serializable {
        @Column(name = "sender_id")
        private Integer senderId;

        @Column(name = "conversation_id")
        private Integer conversationId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConversationMessageId that = (ConversationMessageId) o;
            return Objects.equals(senderId, that.senderId) &&
                    Objects.equals(conversationId, that.conversationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(senderId, conversationId);
        }
    }
} 