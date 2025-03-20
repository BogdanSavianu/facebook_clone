package com.utcn.messagingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "conversation_members")
public class ConversationMember {

    @EmbeddedId
    private ConversationMemberId id;

    @ManyToOne
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(name = "member_id", insertable = false, updatable = false)
    private Integer memberId;

    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Getter
    @Setter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConversationMemberId implements Serializable {
        @Column(name = "conversation_id")
        private Integer conversationId;

        @Column(name = "member_id")
        private Integer memberId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConversationMemberId that = (ConversationMemberId) o;
            return Objects.equals(conversationId, that.conversationId) &&
                    Objects.equals(memberId, that.memberId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(conversationId, memberId);
        }
    }
} 