package com.utcn.contentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.entity.PostableVote;
import com.utcn.contentservice.repository.PostableRepository;
import com.utcn.contentservice.repository.PostableVoteRepository;
import com.utcn.contentservice.service.PostableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PostableVoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostableRepository postableRepository;

    @Autowired
    private PostableVoteRepository postableVoteRepository;

    @Autowired
    private PostableService postableService;

    private Postable testPost;
    private PostableVote testVote;

    @BeforeEach
    void setUp() {
        // Delete all existing votes and posts
        postableVoteRepository.deleteAll();
        postableRepository.deleteAll();

        // Create a test post
        testPost = new Postable();
        testPost.setUserId(1);
        testPost.setTitle("Test Post");
        testPost.setBody("Test Body");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
        testPost.setComments(new ArrayList<>());
        testPost.setVotes(new ArrayList<>());
        testPost.setTags(new ArrayList<>());
        testPost = postableRepository.save(testPost);

        // Create a test vote
        testVote = new PostableVote();
        testVote.setUserId(2);
        testVote.setPostable(testPost);
        testVote.setValue(1);
        testVote.setCreatedAt(LocalDateTime.now());
        testVote.setUpdatedAt(LocalDateTime.now());
        testVote = postableVoteRepository.save(testVote);
    }

    @Test
    void getVotesByPostId_ShouldReturnListOfVotes() throws Exception {
        mockMvc.perform(get("/votes/posts/{postId}", testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testVote.getId()))
                .andExpect(jsonPath("$[0].userId").value(testVote.getUserId()))
                .andExpect(jsonPath("$[0].value").value(testVote.getValue()));
    }

    @Test
    void voteOnPost_ShouldReturnVoteCount() throws Exception {
        mockMvc.perform(post("/votes/posts/{postId}", testPost.getId())
                .param("userId", "3")
                .param("value", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(2));
    }

    @Test
    void voteOnPost_WithInvalidValue_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/votes/posts/{postId}", testPost.getId())
                .param("userId", "3")
                .param("value", "2")) // Invalid vote value
                .andExpect(status().isBadRequest());
    }

    @Test
    void voteOnPost_WithSameValue_ShouldToggleVote() throws Exception {
        mockMvc.perform(post("/votes/posts/{postId}", testPost.getId())
                .param("userId", "2") // Same user as testVote
                .param("value", "1")) // Same value as testVote
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(0)); // Vote should be removed
    }

    @Test
    void voteOnPost_WithDifferentValue_ShouldUpdateVote() throws Exception {
        mockMvc.perform(post("/votes/posts/{postId}", testPost.getId())
                .param("userId", "2") // Same user as testVote
                .param("value", "-1")) // Different value from testVote
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(-1)); // Vote should be updated
    }

    @Test
    void getUserPostVotes_ShouldReturnUserVotes() throws Exception {
        mockMvc.perform(get("/votes/users/{userId}/posts", testVote.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testVote.getId()))
                .andExpect(jsonPath("$[0].userId").value(testVote.getUserId()))
                .andExpect(jsonPath("$[0].value").value(testVote.getValue()));
    }
} 