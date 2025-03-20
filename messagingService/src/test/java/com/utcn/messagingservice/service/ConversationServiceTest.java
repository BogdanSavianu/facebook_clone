package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.repository.ConversationRepository;
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
public class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1);
        testConversation.setConversationName("Test Conversation");
    }

    @Test
    void getAllConversations_ShouldReturnListOfConversations() {
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationRepository.findAll()).thenReturn(conversations);

        List<Conversation> result = conversationService.getAllConversations();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testConversation.getConversationName(), result.get(0).getConversationName());
    }

    @Test
    void getConversationById_WhenConversationExists_ShouldReturnConversation() {
        when(conversationRepository.findById(1)).thenReturn(Optional.of(testConversation));

        Conversation result = conversationService.getConversationById(1);

        assertNotNull(result);
        assertEquals(testConversation.getConversationName(), result.getConversationName());
    }

    @Test
    void getConversationById_WhenConversationDoesNotExist_ShouldReturnNull() {
        when(conversationRepository.findById(1)).thenReturn(Optional.empty());

        Conversation result = conversationService.getConversationById(1);

        assertNull(result);
    }

    @Test
    void searchConversationsByName_ShouldReturnMatchingConversations() {
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationRepository.findByConversationNameContaining("Test")).thenReturn(conversations);

        List<Conversation> result = conversationService.searchConversationsByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testConversation.getConversationName(), result.get(0).getConversationName());
    }

    @Test
    void createConversation_ShouldReturnSavedConversation() {
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = conversationService.createConversation(testConversation);

        assertNotNull(result);
        assertEquals(testConversation.getConversationName(), result.getConversationName());
    }

    @Test
    void updateConversation_ShouldReturnUpdatedConversation() {
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = conversationService.updateConversation(testConversation);

        assertNotNull(result);
        assertEquals(testConversation.getConversationName(), result.getConversationName());
    }

    @Test
    void deleteConversation_ShouldCallRepositoryDelete() {
        doNothing().when(conversationRepository).deleteById(1);

        conversationService.deleteConversation(1);

        verify(conversationRepository, times(1)).deleteById(1);
    }
} 