package com.utcn.messagingservice.repository;

import com.utcn.messagingservice.entity.ConversationMember;
import com.utcn.messagingservice.entity.ConversationMember.ConversationMemberId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMemberRepository extends CrudRepository<ConversationMember, ConversationMemberId> {
    List<ConversationMember> findByIdConversationId(Integer conversationId);
    List<ConversationMember> findByIdMemberId(Integer memberId);
    List<ConversationMember> findByIdMemberIdAndIsAdmin(Integer memberId, Boolean isAdmin);
} 