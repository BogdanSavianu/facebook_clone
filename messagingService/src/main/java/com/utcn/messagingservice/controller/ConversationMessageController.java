package com.utcn.messagingservice.controller;

import com.utcn.messagingservice.entity.ConversationMessage;
import com.utcn.messagingservice.entity.ConversationMessage.ConversationMessageId;
import com.utcn.messagingservice.service.ConversationMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversation-messages")
public class ConversationMessageController {

    @Autowired
    private ConversationMessageService conversationMessageService;

    @GetMapping
    public ResponseEntity<List<ConversationMessage>> getAllMessages() {
        return ResponseEntity.ok(conversationMessageService.getAllMessages());
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<ConversationMessage>> getMessagesByConversationId(@PathVariable Integer conversationId) {
        return ResponseEntity.ok(conversationMessageService.getMessagesByConversationId(conversationId));
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<ConversationMessage>> getMessagesBySenderId(@PathVariable Integer senderId) {
        return ResponseEntity.ok(conversationMessageService.getMessagesBySenderId(senderId));
    }

    @PostMapping
    public ResponseEntity<ConversationMessage> sendMessage(@RequestBody ConversationMessage message) {
        return new ResponseEntity<>(conversationMessageService.sendMessage(message), HttpStatus.CREATED);
    }

    @PutMapping("/status")
    public ResponseEntity<ConversationMessage> updateMessageStatus(@RequestBody ConversationMessage message) {
        ConversationMessage existingMessage = conversationMessageService.getMessageById(message.getId());
        if (existingMessage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(conversationMessageService.updateMessageStatus(message));
    }

    @DeleteMapping("/{senderId}/{conversationId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer senderId, @PathVariable Integer conversationId) {
        ConversationMessageId id = new ConversationMessageId(senderId, conversationId);
        ConversationMessage existingMessage = conversationMessageService.getMessageById(id);
        if (existingMessage == null) {
            return ResponseEntity.notFound().build();
        }
        conversationMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
} 