package com.utcn.contentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utcn.contentservice.config.TestConfig;
import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.repository.PostableRepository;
import com.utcn.contentservice.service.PostableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
public class PostableControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostableService postableService;

    @Autowired
    private PostableRepository postableRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Postable testPost;
    private Postable testComment;

    @BeforeEach
    void setUp() {
        postableRepository.deleteAll();
        
        // Create test post
        testPost = new Postable();
        testPost.setUserId(1);
        testPost.setTitle("Test Post");
        testPost.setBody("Test Post Body");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
        testPost = postableService.addPost(testPost);

        // Create test comment
        testComment = new Postable();
        testComment.setUserId(2);
        testComment.setBody("Test Comment");
        testComment.setParent(testPost);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
        testComment = postableService.addPost(testComment);
    }

    @Test
    void getAllPosts_ShouldReturnListOfPosts() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Post")))
                .andExpect(jsonPath("$[0].body", is("Test Post Body")));
    }

    @Test
    void getPostById_WithValidId_ShouldReturnPost() throws Exception {
        mockMvc.perform(get("/posts/{id}", testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPost.getId())))
                .andExpect(jsonPath("$.title", is("Test Post")))
                .andExpect(jsonPath("$.body", is("Test Post Body")));
    }

    @Test
    void getPostById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllComments_ShouldReturnListOfComments() throws Exception {
        mockMvc.perform(get("/posts/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].body", is("Test Comment")));
    }

    @Test
    void getCommentById_WithValidId_ShouldReturnComment() throws Exception {
        mockMvc.perform(get("/posts/comments/{id}", testComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testComment.getId())))
                .andExpect(jsonPath("$.body", is("Test Comment")));
    }

    @Test
    void getCommentsByPostId_ShouldReturnCommentsForPost() throws Exception {
        mockMvc.perform(get("/posts/post/{postId}/comments", testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].body", is("Test Comment")));
    }

    @Test
    void createPost_ShouldReturnCreatedPost() throws Exception {
        Postable newPost = new Postable();
        newPost.setUserId(3);
        newPost.setTitle("New Post");
        newPost.setBody("New Post Body");

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Post")))
                .andExpect(jsonPath("$.body", is("New Post Body")));
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        Postable newComment = new Postable();
        newComment.setUserId(3);
        newComment.setBody("New Comment");

        mockMvc.perform(post("/posts/{id}/comments", testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body", is("New Comment")));
    }

    @Test
    void updatePost_WithValidUser_ShouldUpdatePost() throws Exception {
        testPost.setBody("Updated Body");

        mockMvc.perform(put("/posts/{id}", testPost.getId())
                .header("X-User-ID", testPost.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body", is("Updated Body")));
    }

    @Test
    void updatePost_WithInvalidUser_ShouldReturn403() throws Exception {
        testPost.setBody("Updated Body");

        mockMvc.perform(put("/posts/{id}", testPost.getId())
                .header("X-User-ID", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPost)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePost_WithValidUser_ShouldDeletePost() throws Exception {
        mockMvc.perform(delete("/posts/{id}", testPost.getId())
                .header("X-User-ID", testPost.getUserId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/posts/{id}", testPost.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePost_WithInvalidUser_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/posts/{id}", testPost.getId())
                .header("X-User-ID", 999))
                .andExpect(status().isForbidden());
    }
} 