package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.entity.ConversationMember;
import com.utcn.messagingservice.entity.ConversationMember.ConversationMemberId;
import com.utcn.messagingservice.repository.ConversationMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConversationMemberServiceTest {

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @InjectMocks
    private ConversationMemberService conversationMemberService;

    private ConversationMember testMember;
    private ConversationMemberId testMemberId;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1);
        testConversation.setConversationName("Test Conversation");

        testMemberId = new ConversationMemberId(1, 1);
        testMember = new ConversationMember();
        testMember.setId(testMemberId);
        testMember.setConversation(testConversation);
        testMember.setMemberId(1);
        testMember.setIsAdmin(false);
    }

    @Test
    void getAllConversationMembers_ShouldReturnListOfMembers() {
        List<ConversationMember> members = Arrays.asList(testMember);
        when(conversationMemberRepository.findAll()).thenReturn(members);

        List<ConversationMember> result = conversationMemberService.getAllConversationMembers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember.getMemberId(), result.get(0).getMemberId());
    }

    @Test
    void getConversationMemberById_WhenMemberExists_ShouldReturnMember() {
        when(conversationMemberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));

        ConversationMember result = conversationMemberService.getConversationMemberById(testMemberId);

        assertNotNull(result);
        assertEquals(testMember.getMemberId(), result.getMemberId());
    }

    @Test
    void getConversationMemberById_WhenMemberDoesNotExist_ShouldReturnNull() {
        when(conversationMemberRepository.findById(testMemberId)).thenReturn(Optional.empty());

        ConversationMember result = conversationMemberService.getConversationMemberById(testMemberId);

        assertNull(result);
    }

    @Test
    void getConversationMembersByConversationId_ShouldReturnMatchingMembers() {
        List<ConversationMember> members = Arrays.asList(testMember);
        when(conversationMemberRepository.findByIdConversationId(1)).thenReturn(members);

        List<ConversationMember> result = conversationMemberService.getConversationMembersByConversationId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember.getMemberId(), result.get(0).getMemberId());
    }

    @Test
    void getConversationsByMemberId_ShouldReturnMatchingMembers() {
        List<ConversationMember> members = Arrays.asList(testMember);
        when(conversationMemberRepository.findByIdMemberId(1)).thenReturn(members);

        List<ConversationMember> result = conversationMemberService.getConversationsByMemberId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember.getMemberId(), result.get(0).getMemberId());
    }

    @Test
    void getAdminsByMemberId_ShouldReturnMatchingMembers() {
        testMember.setIsAdmin(true);
        List<ConversationMember> members = Arrays.asList(testMember);
        when(conversationMemberRepository.findByIdMemberIdAndIsAdmin(1, true)).thenReturn(members);

        List<ConversationMember> result = conversationMemberService.getAdminsByMemberId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember.getMemberId(), result.get(0).getMemberId());
        assertTrue(result.get(0).getIsAdmin());
    }

    @Test
    void addConversationMember_ShouldReturnSavedMember() {
        when(conversationMemberRepository.save(any(ConversationMember.class))).thenReturn(testMember);

        ConversationMember result = conversationMemberService.addConversationMember(testMember);

        assertNotNull(result);
        assertEquals(testMember.getMemberId(), result.getMemberId());
    }

    @Test
    void updateConversationMember_ShouldReturnUpdatedMember() {
        when(conversationMemberRepository.save(any(ConversationMember.class))).thenReturn(testMember);

        ConversationMember result = conversationMemberService.updateConversationMember(testMember);

        assertNotNull(result);
        assertEquals(testMember.getMemberId(), result.getMemberId());
    }

    @Test
    void deleteConversationMember_ShouldCallRepositoryDelete() {
        doNothing().when(conversationMemberRepository).deleteById(testMemberId);

        conversationMemberService.deleteConversationMember(testMemberId);

        verify(conversationMemberRepository, times(1)).deleteById(testMemberId);
    }
} 