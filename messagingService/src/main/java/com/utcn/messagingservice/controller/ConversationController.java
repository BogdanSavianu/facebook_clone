package com.utcn.messagingservice.controller;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<Conversation>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable Integer id) {
        Conversation conversation = conversationService.getConversationById(id);
        if (conversation != null) {
            return ResponseEntity.ok(conversation);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Conversation>> searchConversationsByName(@RequestParam String name) {
        return ResponseEntity.ok(conversationService.searchConversationsByName(name));
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(@RequestBody Conversation conversation) {
        return new ResponseEntity<>(conversationService.createConversation(conversation), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conversation> updateConversation(@PathVariable Integer id, @RequestBody Conversation conversation) {
        Conversation existingConversation = conversationService.getConversationById(id);
        if (existingConversation == null) {
            return ResponseEntity.notFound().build();
        }
        conversation.setId(id);
        return ResponseEntity.ok(conversationService.updateConversation(conversation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Integer id) {
        Conversation existingConversation = conversationService.getConversationById(id);
        if (existingConversation == null) {
            return ResponseEntity.notFound().build();
        }
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
} 