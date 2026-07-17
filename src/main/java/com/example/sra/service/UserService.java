package com.example.sra.service;

import com.example.sra.entity.User;
import com.example.sra.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User registerUser(User user) {
        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Set default role
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public User registerAdmin(User user) {
        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Set default role for admin
        user.setRole("ROLE_ADMIN");
        return userRepository.save(user);
    }

    public long countUsers() {
        return userRepository.countByRole("ROLE_USER");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
