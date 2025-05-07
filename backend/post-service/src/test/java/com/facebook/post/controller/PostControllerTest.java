package com.facebook.post.controller;

import com.facebook.post.model.PostStatus;
import com.facebook.post.payload.PostDTO;
import com.facebook.post.payload.TagDTO;
import com.facebook.post.payload.UserDTO;
import com.facebook.post.payload.request.PostRequest;
import com.facebook.post.service.PostService;
import com.facebook.post.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostDTO testPost;
    private List<PostDTO> postList;

    @BeforeEach
    public void setup() {
        TagDTO tag = new TagDTO(1L, "test");
        
        testPost = new PostDTO(
                1L,
                1L,
                "testuser",
                "Test Post",
                "This is a test post content",
                LocalDateTime.now(),
                "image-url.jpg",
                PostStatus.JUST_POSTED,
                0,
                Collections.singleton(tag)
        );
        
        postList = new ArrayList<>(List.of(testPost));

        UserDTO mockUser = new UserDTO(1L, "user", "user@example.com", 0.0, Collections.singletonList("ROLE_USER"), false);
        when(userService.getUserByUsername("user")).thenReturn(mockUser);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllPosts() throws Exception {
        Pageable defaultPageable = PageRequest.of(0, 10);
        Page<PostDTO> postPage = new PageImpl<>(postList, defaultPageable, postList.size());
        
        when(postService.getAllPostsFiltered(any(Pageable.class), eq(null), eq(null), eq(null)))
                .thenReturn(postPage);

        mockMvc.perform(get("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Post"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetPostById() throws Exception {
        when(postService.getPostById(1L)).thenReturn(testPost);

        mockMvc.perform(get("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreatePost() throws Exception {
        PostRequest postRequest = new PostRequest(
                "Test Post",
                "This is a test post content",
                "image-url.jpg",
                new HashSet<>(Collections.singletonList("test"))
        );

        when(postService.createPost(eq(1L), any(PostRequest.class))).thenReturn(testPost);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    public void testGetPublicPostById() throws Exception {
        when(postService.getPostById(1L)).thenReturn(testPost);

        mockMvc.perform(get("/api/posts/public/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }
} 