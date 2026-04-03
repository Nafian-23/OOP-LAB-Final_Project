package com.foodapp.service;

import com.foodapp.model.Role;
import com.foodapp.model.User;
import com.foodapp.repository.UserRepositoryDB;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class UserService {
    private final UserRepositoryDB userRepository;

    public UserService(UserRepositoryDB userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String fullName, String address, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        String id = UUID.randomUUID().toString();
        String passwordHash = hashPassword(password);
        User user = new User(id, username, passwordHash, fullName, address, role);
        userRepository.save(user);
        return user;
    }

    public User login(String username, String password) {
        String passwordHash = hashPassword(password);
        return userRepository.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(passwordHash))
                .orElseThrow(() -> new AuthException("Invalid username or password"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
