package com.utcn.socialgraphservice.service;

import com.utcn.socialgraphservice.entity.Friendship;
import com.utcn.socialgraphservice.entity.Friendship.FriendshipId;
import com.utcn.socialgraphservice.repository.FriendshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    private Friendship testFriendship;
    private FriendshipId testFriendshipId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testFriendshipId = new FriendshipId(1, 2);
        
        testFriendship = new Friendship();
        testFriendship.setId(testFriendshipId);
        testFriendship.setRequesterId(1);
        testFriendship.setAddresseeId(2);
        testFriendship.setStatus("PENDING");
        testFriendship.setStartedAt(LocalDate.now());
    }

    @Test
    void getAllFriendships_ShouldReturnAllFriendships() {
        List<Friendship> expectedFriendships = Arrays.asList(testFriendship);
        when(friendshipRepository.findAll()).thenReturn(expectedFriendships);

        List<Friendship> actualFriendships = friendshipService.getAllFriendships();

        assertEquals(expectedFriendships, actualFriendships);
    }

    @Test
    void getFriendshipById_WhenFriendshipExists_ShouldReturnFriendship() {
        when(friendshipRepository.findById(testFriendshipId)).thenReturn(Optional.of(testFriendship));

        Friendship actualFriendship = friendshipService.getFriendshipById(testFriendshipId);

        assertNotNull(actualFriendship);
        assertEquals(testFriendship, actualFriendship);
    }

    @Test
    void getFriendshipById_WhenFriendshipDoesNotExist_ShouldReturnNull() {
        FriendshipId nonExistentId = new FriendshipId(999, 888);
        when(friendshipRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Friendship actualFriendship = friendshipService.getFriendshipById(nonExistentId);

        assertNull(actualFriendship);
    }

    @Test
    void getFriendshipsByRequesterId_ShouldReturnFriendships() {
        List<Friendship> expectedFriendships = Arrays.asList(testFriendship);
        when(friendshipRepository.findByRequesterId(1)).thenReturn(expectedFriendships);

        List<Friendship> actualFriendships = friendshipService.getFriendshipsByRequesterId(1);

        assertEquals(expectedFriendships, actualFriendships);
    }

    @Test
    void getFriendshipsByAddresseeId_ShouldReturnFriendships() {
        List<Friendship> expectedFriendships = Arrays.asList(testFriendship);
        when(friendshipRepository.findByAddresseeId(2)).thenReturn(expectedFriendships);

        List<Friendship> actualFriendships = friendshipService.getFriendshipsByAddresseeId(2);

        assertEquals(expectedFriendships, actualFriendships);
    }

    @Test
    void getPendingFriendRequests_ShouldReturnPendingFriendships() {
        List<Friendship> expectedFriendships = Arrays.asList(testFriendship);
        when(friendshipRepository.findByAddresseeIdAndStatus(2, "PENDING")).thenReturn(expectedFriendships);

        List<Friendship> actualFriendships = friendshipService.getPendingFriendRequests(2);

        assertEquals(expectedFriendships, actualFriendships);
    }

    @Test
    void getAcceptedFriendships_ShouldReturnAcceptedFriendships() {
        List<Friendship> sentFriendships = Arrays.asList(testFriendship);
        List<Friendship> receivedFriendships = Arrays.asList(testFriendship);
        when(friendshipRepository.findByRequesterIdAndStatus(1, "ACCEPTED")).thenReturn(sentFriendships);
        when(friendshipRepository.findByAddresseeIdAndStatus(1, "ACCEPTED")).thenReturn(receivedFriendships);

        List<Friendship> actualFriendships = friendshipService.getAcceptedFriendships(1);

        assertEquals(2, actualFriendships.size());
    }

    @Test
    void sendFriendRequest_ShouldSaveAndReturnFriendship() {
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(testFriendship);

        Friendship savedFriendship = friendshipService.sendFriendRequest(testFriendship);

        assertNotNull(savedFriendship);
        assertEquals("PENDING", savedFriendship.getStatus());
        assertNotNull(savedFriendship.getStartedAt());
    }

    @Test
    void updateFriendshipStatus_WhenFriendshipExists_ShouldUpdateAndReturnFriendship() {
        when(friendshipRepository.findById(testFriendshipId)).thenReturn(Optional.of(testFriendship));
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(testFriendship);

        Friendship updatedFriendship = friendshipService.updateFriendshipStatus(testFriendshipId, "ACCEPTED");

        assertNotNull(updatedFriendship);
        assertEquals("ACCEPTED", updatedFriendship.getStatus());
        assertNotNull(updatedFriendship.getStartedAt());
    }

    @Test
    void updateFriendshipStatus_WhenFriendshipDoesNotExist_ShouldReturnNull() {
        FriendshipId nonExistentId = new FriendshipId(999, 888);
        when(friendshipRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Friendship updatedFriendship = friendshipService.updateFriendshipStatus(nonExistentId, "ACCEPTED");

        assertNull(updatedFriendship);
    }

    @Test
    void deleteFriendship_ShouldDeleteFriendship() {
        doNothing().when(friendshipRepository).deleteById(testFriendshipId);

        friendshipService.deleteFriendship(testFriendshipId);

        verify(friendshipRepository).deleteById(testFriendshipId);
    }
} 