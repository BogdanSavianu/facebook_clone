package com.utcn.messagingservice.service;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.entity.ConversationMessage;
import com.utcn.messagingservice.entity.ConversationMessage.ConversationMessageId;
import com.utcn.messagingservice.repository.ConversationMessageRepository;
import com.utcn.messagingservice.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConversationMessageServiceTest {

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationMessageService conversationMessageService;

    private ConversationMessage testMessage;
    private ConversationMessageId testMessageId;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1);
        testConversation.setConversationName("Test Conversation");

        testMessageId = new ConversationMessageId(1, 1);
        testMessage = new ConversationMessage();
        testMessage.setId(testMessageId);
        testMessage.setConversation(testConversation);
        testMessage.setSentAt(LocalDateTime.now());
        testMessage.setStatus("SENT");

        // Set up conversation repository behavior
        when(conversationRepository.findById(any(Integer.class))).thenReturn(Optional.of(testConversation));
    }

    @Test
    void getAllMessages_ShouldReturnListOfMessages() {
        List<ConversationMessage> messages = Arrays.asList(testMessage);
        when(conversationMessageRepository.findAll()).thenReturn(messages);

        List<ConversationMessage> result = conversationMessageService.getAllMessages();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getStatus(), result.get(0).getStatus());
    }

    @Test
    void getMessageById_WhenMessageExists_ShouldReturnMessage() {
        when(conversationMessageRepository.findById(testMessageId)).thenReturn(Optional.of(testMessage));

        ConversationMessage result = conversationMessageService.getMessageById(testMessageId);

        assertNotNull(result);
        assertEquals(testMessage.getStatus(), result.getStatus());
    }

    @Test
    void getMessageById_WhenMessageDoesNotExist_ShouldReturnNull() {
        when(conversationMessageRepository.findById(testMessageId)).thenReturn(Optional.empty());

        ConversationMessage result = conversationMessageService.getMessageById(testMessageId);

        assertNull(result);
    }

    @Test
    void getMessagesByConversationId_ShouldReturnMatchingMessages() {
        List<ConversationMessage> messages = Arrays.asList(testMessage);
        when(conversationMessageRepository.findByIdConversationIdOrderBySentAtDesc(1)).thenReturn(messages);

        List<ConversationMessage> result = conversationMessageService.getMessagesByConversationId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getStatus(), result.get(0).getStatus());
    }

    @Test
    void getMessagesBySenderId_ShouldReturnMatchingMessages() {
        List<ConversationMessage> messages = Arrays.asList(testMessage);
        when(conversationMessageRepository.findByIdSenderId(1)).thenReturn(messages);

        List<ConversationMessage> result = conversationMessageService.getMessagesBySenderId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getStatus(), result.get(0).getStatus());
    }

    @Test
    void sendMessage_WithoutSentAtAndStatus_ShouldSetDefaultsAndSave() {
        ConversationMessage newMessage = new ConversationMessage();
        newMessage.setId(testMessageId);
        newMessage.setConversation(testConversation);

        when(conversationMessageRepository.save(any(ConversationMessage.class))).thenAnswer(invocation -> {
            ConversationMessage savedMessage = invocation.getArgument(0);
            assertNotNull(savedMessage.getSentAt());
            assertEquals("SENT", savedMessage.getStatus());
            return savedMessage;
        });

        ConversationMessage result = conversationMessageService.sendMessage(newMessage);

        assertNotNull(result);
    }

    @Test
    void sendMessage_WithExistingSentAtAndStatus_ShouldPreserveValues() {
        LocalDateTime sentAt = LocalDateTime.now().minusHours(1);
        testMessage.setSentAt(sentAt);
        testMessage.setStatus("DELIVERED");

        when(conversationMessageRepository.save(any(ConversationMessage.class))).thenReturn(testMessage);

        ConversationMessage result = conversationMessageService.sendMessage(testMessage);

        assertNotNull(result);
        assertEquals(sentAt, result.getSentAt());
        assertEquals("DELIVERED", result.getStatus());
    }

    @Test
    void updateMessageStatus_ShouldReturnUpdatedMessage() {
        testMessage.setStatus("READ");
        when(conversationMessageRepository.findById(testMessageId)).thenReturn(Optional.of(testMessage));
        when(conversationMessageRepository.save(any(ConversationMessage.class))).thenReturn(testMessage);

        ConversationMessage result = conversationMessageService.updateMessageStatus(testMessage);

        assertNotNull(result);
        assertEquals("READ", result.getStatus());
    }
} 