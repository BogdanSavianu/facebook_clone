package com.facebook.user.service;

import com.facebook.user.payload.UserDTO;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    List<UserDTO> getAllUsers();
    
    UserDTO getUserById(Long id);
    
    UserDTO getUserByUsername(String username);
    
    UserDTO updateUserScore(Long userId, Double scoreChange);

    UserDTO banUser(Long userId);
    UserDTO unbanUser(Long userId);
    UserDTO addRoleToUser(Long userId, String role);
    UserDTO removeRoleFromUser(Long userId, String role);

    UserDTO assignModeratorRoleToCurrentUser();

    Page<UserDTO> searchUsers(String query, Pageable pageable);
} 