package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.ConversationMember;
import com.utcn.messagingservice.entity.ConversationMember.ConversationMemberId;
import com.utcn.messagingservice.repository.ConversationMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationMemberService {

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    public List<ConversationMember> getAllConversationMembers() {
        List<ConversationMember> members = new ArrayList<>();
        conversationMemberRepository.findAll().forEach(members::add);
        return members;
    }

    public ConversationMember getConversationMemberById(ConversationMemberId id) {
        Optional<ConversationMember> member = conversationMemberRepository.findById(id);
        return member.orElse(null);
    }

    public List<ConversationMember> getConversationMembersByConversationId(Integer conversationId) {
        return conversationMemberRepository.findByIdConversationId(conversationId);
    }

    public List<ConversationMember> getConversationsByMemberId(Integer memberId) {
        return conversationMemberRepository.findByIdMemberId(memberId);
    }

    public List<ConversationMember> getAdminsByMemberId(Integer memberId) {
        return conversationMemberRepository.findByIdMemberIdAndIsAdmin(memberId, true);
    }

    public ConversationMember addConversationMember(ConversationMember member) {
        return conversationMemberRepository.save(member);
    }

    public ConversationMember updateConversationMember(ConversationMember member) {
        return conversationMemberRepository.save(member);
    }

    public void deleteConversationMember(ConversationMemberId id) {
        conversationMemberRepository.deleteById(id);
    }
} 