package com.utcn.userservice.service;

import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> retrieveUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public User retrieveUserById(Integer id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }
    
    public User retrieveUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public String deleteUserById(Integer id) {
        userRepository.deleteById(id);
        return "User deleted successfully";
    }
} 