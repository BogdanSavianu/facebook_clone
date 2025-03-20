package com.utcn.messagingservice.controller;

import com.utcn.messagingservice.entity.ConversationMember;
import com.utcn.messagingservice.entity.ConversationMember.ConversationMemberId;
import com.utcn.messagingservice.service.ConversationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversation-members")
public class ConversationMemberController {

    @Autowired
    private ConversationMemberService conversationMemberService;

    @GetMapping
    public ResponseEntity<List<ConversationMember>> getAllConversationMembers() {
        return ResponseEntity.ok(conversationMemberService.getAllConversationMembers());
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<ConversationMember>> getConversationMembersByConversationId(@PathVariable Integer conversationId) {
        return ResponseEntity.ok(conversationMemberService.getConversationMembersByConversationId(conversationId));
    }

    @GetMapping("/user/{memberId}")
    public ResponseEntity<List<ConversationMember>> getConversationsByMemberId(@PathVariable Integer memberId) {
        return ResponseEntity.ok(conversationMemberService.getConversationsByMemberId(memberId));
    }

    @GetMapping("/admins/{memberId}")
    public ResponseEntity<List<ConversationMember>> getAdminsByMemberId(@PathVariable Integer memberId) {
        return ResponseEntity.ok(conversationMemberService.getAdminsByMemberId(memberId));
    }

    @PostMapping
    public ResponseEntity<ConversationMember> createConversationMember(@RequestBody ConversationMember member) {
        return new ResponseEntity<>(conversationMemberService.addConversationMember(member), HttpStatus.CREATED);
    }

    @PutMapping("/{conversationId}/{memberId}")
    public ResponseEntity<ConversationMember> updateMember(
            @PathVariable Integer conversationId,
            @PathVariable Integer memberId,
            @RequestBody ConversationMember member) {
        ConversationMember.ConversationMemberId memberKey = new ConversationMember.ConversationMemberId();
        memberKey.setConversationId(conversationId);
        memberKey.setMemberId(memberId);
        ConversationMember existingMember = conversationMemberService.getConversationMemberById(memberKey);
        if (existingMember == null) {
            return ResponseEntity.notFound().build();
        }
        member.setId(memberKey);
        return ResponseEntity.ok(conversationMemberService.updateConversationMember(member));
    }

    @DeleteMapping("/{conversationId}/{memberId}")
    public ResponseEntity<Void> deleteConversationMember(@PathVariable Integer conversationId, @PathVariable Integer memberId) {
        ConversationMemberId id = new ConversationMemberId(conversationId, memberId);
        ConversationMember existingMember = conversationMemberService.getConversationMemberById(id);
        if (existingMember == null) {
            return ResponseEntity.notFound().build();
        }
        conversationMemberService.deleteConversationMember(id);
        return ResponseEntity.noContent().build();
    }
} 