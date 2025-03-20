package com.utcn.socialgraphservice.controller;

import com.utcn.socialgraphservice.entity.Friendship;
import com.utcn.socialgraphservice.entity.Friendship.FriendshipId;
import com.utcn.socialgraphservice.repository.FriendshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FriendshipControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private Friendship testFriendship;

    @BeforeEach
    void setUp() {
        friendshipRepository.deleteAll();
        
        testFriendship = new Friendship();
        FriendshipId id = new FriendshipId(1, 2);
        testFriendship.setId(id);
        testFriendship.setRequesterId(1);
        testFriendship.setAddresseeId(2);
        testFriendship.setStatus("PENDING");
        testFriendship.setStartedAt(LocalDate.now());
        friendshipRepository.save(testFriendship);
    }

    @Test
    void whenGetAllFriendships_thenReturnFriendships() throws Exception {
        mockMvc.perform(get("/friendships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requesterId").value(1))
                .andExpect(jsonPath("$[0].addresseeId").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void whenGetFriendshipsByRequesterId_thenReturnMatchingFriendships() throws Exception {
        mockMvc.perform(get("/friendships/requester/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requesterId").value(1))
                .andExpect(jsonPath("$[0].addresseeId").value(2));
    }

    @Test
    void whenGetFriendshipsByAddresseeId_thenReturnMatchingFriendships() throws Exception {
        mockMvc.perform(get("/friendships/addressee/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requesterId").value(1))
                .andExpect(jsonPath("$[0].addresseeId").value(2));
    }

    @Test
    void whenGetPendingFriendRequests_thenReturnPendingFriendships() throws Exception {
        mockMvc.perform(get("/friendships/pending/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void whenGetAcceptedFriendships_thenReturnAcceptedFriendships() throws Exception {
        testFriendship.setStatus("ACCEPTED");
        friendshipRepository.save(testFriendship);

        mockMvc.perform(get("/friendships/accepted/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    void whenSendFriendRequest_thenCreateFriendship() throws Exception {
        Friendship newFriendship = new Friendship();
        FriendshipId id = new FriendshipId(3, 4);
        newFriendship.setId(id);
        newFriendship.setRequesterId(3);
        newFriendship.setAddresseeId(4);

        mockMvc.perform(post("/friendships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":{\"idRequester\":3,\"idAddressee\":4},\"requesterId\":3,\"addresseeId\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void whenUpdateFriendshipStatus_thenUpdateStatus() throws Exception {
        mockMvc.perform(put("/friendships/1/2/status")
                .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void whenDeleteFriendship_thenDeleteFriendship() throws Exception {
        mockMvc.perform(delete("/friendships/1/2"))
                .andExpect(status().isNoContent());

        List<Friendship> friendships = new ArrayList<>();
        friendshipRepository.findAll().forEach(friendships::add);
        assertTrue(friendships.isEmpty());
    }

    @Test
    void whenUpdateNonExistentFriendshipStatus_thenReturnNotFound() throws Exception {
        mockMvc.perform(put("/friendships/999/888/status")
                .param("status", "ACCEPTED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteNonExistentFriendship_thenReturnNotFound() throws Exception {
        mockMvc.perform(delete("/friendships/999/888"))
                .andExpect(status().isNotFound());
    }
} 