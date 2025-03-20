package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.entity.ConversationMessage;
import com.utcn.messagingservice.entity.ConversationMessage.ConversationMessageId;
import com.utcn.messagingservice.repository.ConversationMessageRepository;
import com.utcn.messagingservice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationMessageService {

    @Autowired
    private ConversationMessageRepository conversationMessageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    public List<ConversationMessage> getAllMessages() {
        List<ConversationMessage> messages = new ArrayList<>();
        conversationMessageRepository.findAll().forEach(messages::add);
        return messages;
    }

    public ConversationMessage getMessageById(ConversationMessageId id) {
        Optional<ConversationMessage> message = conversationMessageRepository.findById(id);
        return message.orElse(null);
    }

    public List<ConversationMessage> getMessagesByConversationId(Integer conversationId) {
        return conversationMessageRepository.findByIdConversationIdOrderBySentAtDesc(conversationId);
    }

    public List<ConversationMessage> getMessagesBySenderId(Integer senderId) {
        return conversationMessageRepository.findByIdSenderId(senderId);
    }

    public ConversationMessage sendMessage(ConversationMessage message) {
        if (message.getSentAt() == null) {
            message.setSentAt(LocalDateTime.now());
        }
        if (message.getStatus() == null) {
            message.setStatus("SENT");
        }
        // Fetch and set the conversation
        Conversation conversation = conversationRepository.findById(message.getId().getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        message.setConversation(conversation);
        return conversationMessageRepository.save(message);
    }

    public ConversationMessage updateMessage(ConversationMessage message) {
        ConversationMessage existingMessage = getMessageById(message.getId());
        if (existingMessage != null) {
            existingMessage.setContent(message.getContent());
            existingMessage.setMedia(message.getMedia());
            existingMessage.setMediaType(message.getMediaType());
            existingMessage.setStatus(message.getStatus());
            existingMessage.setSentAt(LocalDateTime.now());
            return conversationMessageRepository.save(existingMessage);
        }
        return null;
    }

    public ConversationMessage updateMessageStatus(ConversationMessage message) {
        ConversationMessage existingMessage = getMessageById(message.getId());
        if (existingMessage != null) {
            existingMessage.setStatus(message.getStatus());
            return conversationMessageRepository.save(existingMessage);
        }
        return null;
    }

    public void deleteMessage(ConversationMessageId id) {
        conversationMessageRepository.deleteById(id);
    }
} 