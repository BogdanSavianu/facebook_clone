package com.utcn.messagingservice.repository;

import com.utcn.messagingservice.entity.Conversation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends CrudRepository<Conversation, Integer> {
    List<Conversation> findByConversationNameContaining(String name);
} 