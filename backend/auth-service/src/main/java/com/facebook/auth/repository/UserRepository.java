package com.facebook.auth.repository;

import com.facebook.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.banned = :isBanned WHERE u.id = :userId")
    void updateBanStatus(@Param("userId") Long userId, @Param("isBanned") boolean isBanned);
} 