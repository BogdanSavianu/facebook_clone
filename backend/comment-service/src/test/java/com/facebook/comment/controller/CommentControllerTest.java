package com.facebook.comment.controller;

import com.facebook.comment.payload.CommentDTO;
import com.facebook.comment.payload.request.CommentRequest;
import com.facebook.comment.service.CommentService;
import com.facebook.comment.service.UserService;
import com.facebook.comment.payload.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private CommentDTO testComment;
    private List<CommentDTO> commentList;

    @BeforeEach
    public void setup() {
        testComment = new CommentDTO(
                1L,
                1L,
                null,
                1L,
                "testuser",
                10.0,
                "This is a test comment",
                LocalDateTime.now(),
                "image-url.jpg",
                0
        );
        
        commentList = Collections.singletonList(testComment);

        UserDTO mockUser = new UserDTO(1L, "user", "user@example.com", 0.0, Collections.singletonList("ROLE_USER"), false);
        when(userService.getUserByUsername("user")).thenReturn(mockUser);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetCommentsByPostId() throws Exception {
        when(commentService.getCommentsByPostId(1L)).thenReturn(commentList);

        mockMvc.perform(get("/api/comments/post/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("This is a test comment"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetCommentById() throws Exception {
        when(commentService.getCommentById(1L)).thenReturn(testComment);

        mockMvc.perform(get("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("This is a test comment"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreateComment() throws Exception {
        CommentRequest commentRequest = new CommentRequest(
                1L,
                "This is a test comment",
                "image-url.jpg",
                null
        );

        when(commentService.createComment(any(Long.class), any(CommentRequest.class))).thenReturn(testComment);

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("This is a test comment"));
    }

    @Test
    public void testGetPublicCommentsByPostId() throws Exception {
        when(commentService.getCommentsByPostId(1L)).thenReturn(commentList);

        mockMvc.perform(get("/api/comments/public/post/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("This is a test comment"));
    }
} 