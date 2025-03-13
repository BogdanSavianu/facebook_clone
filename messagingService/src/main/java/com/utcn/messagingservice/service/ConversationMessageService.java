package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.ConversationMessage;
import com.utcn.messagingservice.entity.ConversationMessage.ConversationMessageId;
import com.utcn.messagingservice.repository.ConversationMessageRepository;
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
        return conversationMessageRepository.save(message);
    }

    public ConversationMessage updateMessageStatus(ConversationMessage message) {
        return conversationMessageRepository.save(message);
    }

    public void deleteMessage(ConversationMessageId id) {
        conversationMessageRepository.deleteById(id);
    }
} 