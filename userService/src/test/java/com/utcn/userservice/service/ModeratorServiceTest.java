package com.utcn.userservice.service;

import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModeratorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ModeratorService moderatorService;

    private User testModerator;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testModerator = new User();
        testModerator.setId(1);
        testModerator.setIsModerator(true);
        testModerator.setIsBanned(false);

        testUser = new User();
        testUser.setId(2);
        testUser.setIsModerator(false);
        testUser.setIsBanned(false);
    }

    @Test
    void banUser_WhenModeratorAndUserExist_ShouldBanUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(notificationService).sendBanNotification(any(User.class), anyString());

        User bannedUser = moderatorService.banUser(1, 2, "Violation of community guidelines");

        assertNotNull(bannedUser);
        assertTrue(bannedUser.getIsBanned());
        assertEquals("Violation of community guidelines", bannedUser.getBanReason());
        assertNotNull(bannedUser.getBannedAt());
        assertEquals(1, bannedUser.getBannedById());
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(userRepository).save(testUser);
        verify(notificationService).sendBanNotification(testUser, "Violation of community guidelines");
    }

    @Test
    void banUser_WhenModeratorNotFound_ShouldThrowException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> moderatorService.banUser(999, 2, "Test reason"));
        assertEquals("Moderator not found", exception.getMessage());
        verify(userRepository).findById(999);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).sendBanNotification(any(User.class), anyString());
    }

    @Test
    void banUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> moderatorService.banUser(1, 999, "Test reason"));
        assertEquals("User to ban not found", exception.getMessage());
        verify(userRepository).findById(1);
        verify(userRepository).findById(999);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).sendBanNotification(any(User.class), anyString());
    }

    @Test
    void banUser_WhenNotModerator_ShouldThrowException() {
        User nonModerator = new User();
        nonModerator.setId(3);
        nonModerator.setIsModerator(false);
        when(userRepository.findById(3)).thenReturn(Optional.of(nonModerator));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> moderatorService.banUser(3, 2, "Test reason"));
        assertEquals("User is not a moderator", exception.getMessage());
        verify(userRepository).findById(3);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).sendBanNotification(any(User.class), anyString());
    }

    @Test
    void banUser_WhenTargetIsModerator_ShouldThrowException() {
        User anotherModerator = new User();
        anotherModerator.setId(3);
        anotherModerator.setIsModerator(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(3)).thenReturn(Optional.of(anotherModerator));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> moderatorService.banUser(1, 3, "Test reason"));
        assertEquals("Cannot ban another moderator", exception.getMessage());
        verify(userRepository).findById(1);
        verify(userRepository).findById(3);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).sendBanNotification(any(User.class), anyString());
    }

    @Test
    void unbanUser_WhenModeratorAndBannedUserExist_ShouldUnbanUser() {
        User bannedUser = new User();
        bannedUser.setId(2);
        bannedUser.setIsBanned(true);
        bannedUser.setBanReason("Previous violation");
        bannedUser.setBannedAt(LocalDateTime.now().minusDays(1));
        bannedUser.setBannedById(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(bannedUser));
        when(userRepository.save(any(User.class))).thenReturn(bannedUser);

        User unbannedUser = moderatorService.unbanUser(1, 2);

        assertNotNull(unbannedUser);
        assertFalse(unbannedUser.getIsBanned());
        assertNull(unbannedUser.getBanReason());
        assertNull(unbannedUser.getBannedAt());
        assertNull(unbannedUser.getBannedById());
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(userRepository).save(bannedUser);
    }

    @Test
    void unbanUser_WhenUserNotBanned_ShouldThrowException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> moderatorService.unbanUser(1, 2));
        assertEquals("User is not banned", exception.getMessage());
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void promoteToModerator_WhenUserExists_ShouldPromoteUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User promotedUser = moderatorService.promoteToModerator(1, 2);

        assertNotNull(promotedUser);
        assertTrue(promotedUser.getIsModerator());
    }

    @Test
    void demoteFromModerator_WhenModeratorExists_ShouldDemoteUser() {
        User moderatorToDemote = new User();
        moderatorToDemote.setId(2);
        moderatorToDemote.setIsModerator(true);

        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(moderatorToDemote));
        when(userRepository.save(any(User.class))).thenReturn(moderatorToDemote);

        User demotedUser = moderatorService.demoteFromModerator(1, 2);

        assertNotNull(demotedUser);
        assertFalse(demotedUser.getIsModerator());
    }

    @Test
    void demoteFromModerator_WhenUserNotModerator_ShouldThrowException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> moderatorService.demoteFromModerator(1, 2));
        assertEquals("User is not a moderator", exception.getMessage());
    }

    @Test
    void isModerator_WhenUserExists_ShouldReturnCorrectStatus() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testModerator));
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));

        assertTrue(moderatorService.isModerator(1));
        assertFalse(moderatorService.isModerator(2));
    }

    @Test
    void isModerator_WhenUserDoesNotExist_ShouldReturnFalse() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertFalse(moderatorService.isModerator(999));
    }

    @Test
    void isBanned_WhenUserExists_ShouldReturnCorrectStatus() {
        User bannedUser = new User();
        bannedUser.setId(3);
        bannedUser.setIsBanned(true);
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(3)).thenReturn(Optional.of(bannedUser));

        assertFalse(moderatorService.isBanned(2));
        assertTrue(moderatorService.isBanned(3));
    }

    @Test
    void isBanned_WhenUserDoesNotExist_ShouldReturnFalse() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertFalse(moderatorService.isBanned(999));
    }
} 