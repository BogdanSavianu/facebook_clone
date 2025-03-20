package com.utcn.messagingservice.controller;

import com.utcn.messagingservice.entity.Conversation;
import com.utcn.messagingservice.entity.ConversationMessage;
import com.utcn.messagingservice.repository.ConversationMessageRepository;
import com.utcn.messagingservice.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ConversationMessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationMessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private Conversation testConversation;
    private ConversationMessage testMessage;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();

        testConversation = new Conversation();
        testConversation = conversationRepository.save(testConversation);

        testMessage = new ConversationMessage();
        ConversationMessage.ConversationMessageId messageId = new ConversationMessage.ConversationMessageId();
        messageId.setConversationId(testConversation.getId());
        messageId.setSenderId(1);
        testMessage.setId(messageId);
        testMessage.setConversation(testConversation);
        testMessage.setContent("Test message");
        testMessage.setStatus("SENT");
        testMessage.setSentAt(LocalDateTime.now());
        testMessage = messageRepository.save(testMessage);
    }

    @Test
    void whenGetAllMessages_thenReturnMessages() throws Exception {
        mockMvc.perform(get("/conversation-messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test message"))
                .andExpect(jsonPath("$[0].senderId").value(1))
                .andExpect(jsonPath("$[0].id.conversationId").value(testConversation.getId()));
    }

    @Test
    void whenGetMessagesByConversationId_thenReturnMessages() throws Exception {
        mockMvc.perform(get("/conversation-messages/conversation/{conversationId}", testConversation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test message"))
                .andExpect(jsonPath("$[0].senderId").value(1));
    }

    @Test
    void whenGetMessagesBySenderId_thenReturnMessages() throws Exception {
        mockMvc.perform(get("/conversation-messages/sender/{senderId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test message"))
                .andExpect(jsonPath("$[0].id.conversationId").value(testConversation.getId()));
    }

    @Test
    void whenSendMessage_thenCreateMessage() throws Exception {
        ConversationMessage newMessage = new ConversationMessage();
        ConversationMessage.ConversationMessageId messageId = new ConversationMessage.ConversationMessageId();
        messageId.setConversationId(testConversation.getId());
        messageId.setSenderId(2);
        newMessage.setId(messageId);
        newMessage.setConversation(testConversation);
        newMessage.setContent("New test message");
        newMessage.setStatus("SENT");
        newMessage.setSentAt(LocalDateTime.now());

        MvcResult result = mockMvc.perform(post("/conversation-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":{\"conversationId\":" + testConversation.getId() + ",\"senderId\":2},\"content\":\"New test message\",\"status\":\"SENT\",\"sentAt\":\"" + LocalDateTime.now() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        List<ConversationMessage> messages = (List<ConversationMessage>) messageRepository.findAll();
        assertEquals(2, messages.size());
        assertTrue(messages.stream().anyMatch(m -> m.getContent().equals("New test message")));
    }

    @Test
    void whenUpdateMessage_thenUpdateMessage() throws Exception {
        String updatedContent = "Updated test message";
        mockMvc.perform(put("/conversation-messages/{conversationId}/{senderId}", testConversation.getId(), 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + updatedContent + "\",\"status\":\"DELIVERED\",\"sentAt\":\"" + LocalDateTime.now() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(updatedContent))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        ConversationMessage updatedMessage = messageRepository.findById(testMessage.getId()).orElse(null);
        assertNotNull(updatedMessage);
        assertEquals(updatedContent, updatedMessage.getContent());
        assertEquals("DELIVERED", updatedMessage.getStatus());
    }

    @Test
    void whenUpdateNonExistentMessage_thenReturnNotFound() throws Exception {
        mockMvc.perform(put("/conversation-messages/{conversationId}/{senderId}", 999, 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Updated message\",\"status\":\"DELIVERED\",\"sentAt\":\"" + LocalDateTime.now() + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteMessage_thenDeleteMessage() throws Exception {
        mockMvc.perform(delete("/conversation-messages/{senderId}/{conversationId}", 1, testConversation.getId()))
                .andExpect(status().isNoContent());

        assertFalse(messageRepository.existsById(testMessage.getId()));
    }

    @Test
    void whenDeleteNonExistentMessage_thenReturnNotFound() throws Exception {
        mockMvc.perform(delete("/conversation-messages/{senderId}/{conversationId}", 999, 999))
                .andExpect(status().isNotFound());

        assertTrue(messageRepository.existsById(testMessage.getId()));
    }
} 