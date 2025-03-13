package com.utcn.messagingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Entity
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

    @Embeddable
    public static class ConversationMemberId implements Serializable {

        @Column(name = "conversation_id")
        private Integer conversationId;

        @Column(name = "member_id")
        private Integer memberId;

        public ConversationMemberId() {
        }

        public ConversationMemberId(Integer conversationId, Integer memberId) {
            this.conversationId = conversationId;
            this.memberId = memberId;
        }

        public Integer getConversationId() {
            return conversationId;
        }

        public void setConversationId(Integer conversationId) {
            this.conversationId = conversationId;
        }

        public Integer getMemberId() {
            return memberId;
        }

        public void setMemberId(Integer memberId) {
            this.memberId = memberId;
        }

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