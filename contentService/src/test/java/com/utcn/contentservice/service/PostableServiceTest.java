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
        // Setup test post
        testPost = new Postable();
        testPost.setId(1);
        testPost.setUserId(1);
        testPost.setTitle("Test Post");
        testPost.setBody("Test Post Body");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        // Setup test comment
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
        // Arrange
        List<Postable> allPostables = Arrays.asList(testPost, testComment);
        when(postableRepository.findAll()).thenReturn(allPostables);

        // Act
        List<Postable> result = postableService.getAllPosts();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(testPost));
        assertFalse(result.contains(testComment));
    }

    @Test
    void retrievePostById_WithValidId_ShouldReturnPost() {
        // Arrange
        when(postableRepository.findById(1)).thenReturn(Optional.of(testPost));

        // Act
        Optional<Postable> result = postableService.getPostById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPost.getId(), result.get().getId());
        assertEquals(testPost.getTitle(), result.get().getTitle());
    }

    @Test
    void retrieveComments_ShouldReturnOnlyComments() {
        // Arrange
        List<Postable> allPostables = Arrays.asList(testPost, testComment);
        when(postableRepository.findAll()).thenReturn(allPostables);

        // Act
        List<Postable> result = postableService.getAllComments();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(testComment));
        assertFalse(result.contains(testPost));
    }

    @Test
    void retrieveCommentById_WithValidId_ShouldReturnComment() {
        // Arrange
        when(postableRepository.findById(2)).thenReturn(Optional.of(testComment));

        // Act
        Optional<Postable> result = postableService.getCommentById(2);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testComment.getId(), result.get().getId());
        assertNull(result.get().getTitle());
    }

    @Test
    void retrievePostsByUserId_ShouldReturnUserPosts() {
        // Arrange
        List<Postable> userPosts = Arrays.asList(testPost);
        when(postableRepository.findByUserIdAndTitleIsNotNullAndParentIsNull(1))
            .thenReturn(userPosts);

        // Act
        List<Postable> result = postableService.retrievePostsByUserId(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testPost, result.get(0));
    }

    @Test
    void addPost_ShouldSaveAndReturnPost() {
        // Arrange
        when(postableRepository.save(any(Postable.class))).thenReturn(testPost);

        // Act
        Postable result = postableService.addPost(testPost);

        // Assert
        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getTitle(), result.getTitle());
        verify(postableRepository, times(1)).save(any(Postable.class));
    }

    @Test
    void updatePost_ShouldUpdateAndReturnPost() {
        // Arrange
        Postable updatedPost = testPost;
        updatedPost.setTitle("Updated Title");
        when(postableRepository.save(any(Postable.class))).thenReturn(updatedPost);

        // Act
        Postable result = postableService.updatePost(updatedPost);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(postableRepository, times(1)).save(any(Postable.class));
    }

    @Test
    void deletePostById_ShouldCallRepository() {
        // Arrange
        doNothing().when(postableRepository).deleteById(any());

        // Act
        postableService.deletePost(1);

        // Assert
        verify(postableRepository, times(1)).deleteById(1);
    }

    @Test
    void retrieveCommentsByPostId_ShouldReturnComments() {
        // Arrange
        testPost.getComments().add(testComment);
        when(postableRepository.findById(1)).thenReturn(Optional.of(testPost));

        // Act
        List<Postable> result = postableService.getCommentsByPostId(1);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(testComment));
    }
} 