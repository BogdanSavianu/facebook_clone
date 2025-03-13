package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    public List<Conversation> getAllConversations() {
        List<Conversation> conversations = new ArrayList<>();
        conversationRepository.findAll().forEach(conversations::add);
        return conversations;
    }

    public Conversation getConversationById(Integer id) {
        Optional<Conversation> conversation = conversationRepository.findById(id);
        return conversation.orElse(null);
    }

    public List<Conversation> searchConversationsByName(String name) {
        return conversationRepository.findByConversationNameContaining(name);
    }

    public Conversation createConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Conversation updateConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public void deleteConversation(Integer id) {
        conversationRepository.deleteById(id);
    }
} 