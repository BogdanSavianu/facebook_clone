package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.repository.PostableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostableServiceTest {

    @Mock
    private PostableRepository postableRepository;

    @InjectMocks
    private PostableService postableService;

    private Postable testPost;
    private Postable testComment;

    @BeforeEach
    void setUp() {
        testPost = new Postable();
        testPost.setId(1);
        testPost.setUserId(1);
        testPost.setTitle("Test Post");
        testPost.setBody("Test Post Body");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        testComment = new Postable();
        testComment.setId(2);
        testComment.setUserId(2);
        testComment.setBody("Test Comment Body");
        testComment.setParent(testPost);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void retrievePosts_ShouldReturnOnlyPosts() {
        List<Postable> allPostables = Arrays.asList(testPost, testComment);
        when(postableRepository.findAll()).thenReturn(allPostables);

        List<Postable> result = postableService.getAllPosts();

        assertEquals(1, result.size());
        assertTrue(result.contains(testPost));
        assertFalse(result.contains(testComment));
    }

    @Test
    void retrievePostById_WithValidId_ShouldReturnPost() {
        when(postableRepository.findById(1)).thenReturn(Optional.of(testPost));

        Optional<Postable> result = postableService.getPostById(1);

        assertTrue(result.isPresent());
        assertEquals(testPost.getId(), result.get().getId());
        assertEquals(testPost.getTitle(), result.get().getTitle());
    }

    @Test
    void retrieveComments_ShouldReturnOnlyComments() {
        List<Postable> allPostables = Arrays.asList(testPost, testComment);
        when(postableRepository.findAll()).thenReturn(allPostables);

        List<Postable> result = postableService.getAllComments();

        assertEquals(1, result.size());
        assertTrue(result.contains(testComment));
        assertFalse(result.contains(testPost));
    }

    @Test
    void retrieveCommentById_WithValidId_ShouldReturnComment() {
        when(postableRepository.findById(2)).thenReturn(Optional.of(testComment));

        Optional<Postable> result = postableService.getCommentById(2);

        assertTrue(result.isPresent());
        assertEquals(testComment.getId(), result.get().getId());
        assertNull(result.get().getTitle());
    }

    @Test
    void retrievePostsByUserId_ShouldReturnUserPosts() {
        List<Postable> userPosts = Arrays.asList(testPost);
        when(postableRepository.findByUserIdAndTitleIsNotNullAndParentIsNull(1))
            .thenReturn(userPosts);

        List<Postable> result = postableService.retrievePostsByUserId(1);

        assertEquals(1, result.size());
        assertEquals(testPost, result.get(0));
    }

    @Test
    void addPost_ShouldSaveAndReturnPost() {
        when(postableRepository.save(any(Postable.class))).thenReturn(testPost);

        Postable result = postableService.addPost(testPost);

        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getTitle(), result.getTitle());
    }

    @Test
    void updatePost_ShouldUpdateAndReturnPost() {
        Postable updatedPost = testPost;
        updatedPost.setTitle("Updated Title");
        when(postableRepository.save(any(Postable.class))).thenReturn(updatedPost);

        Postable result = postableService.updatePost(updatedPost);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void deletePostById_ShouldCallRepository() {
        doNothing().when(postableRepository).deleteById(any());

        postableService.deletePost(1);

        verify(postableRepository, times(1)).deleteById(1);
    }

    @Test
    void retrieveCommentsByPostId_ShouldReturnComments() {
        testPost.getComments().add(testComment);
        when(postableRepository.findById(1)).thenReturn(Optional.of(testPost));

        List<Postable> result = postableService.getCommentsByPostId(1);

        assertEquals(1, result.size());
        assertTrue(result.contains(testComment));
    }
} 