package com.utcn.messagingservice.repository;

import com.utcn.messagingservice.entity.ConversationMessage;
import com.utcn.messagingservice.entity.ConversationMessage.ConversationMessageId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends CrudRepository<ConversationMessage, ConversationMessageId> {
    List<ConversationMessage> findByIdConversationId(Integer conversationId);
    List<ConversationMessage> findByIdSenderId(Integer senderId);
    List<ConversationMessage> findByIdConversationIdOrderBySentAtDesc(Integer conversationId);
} 