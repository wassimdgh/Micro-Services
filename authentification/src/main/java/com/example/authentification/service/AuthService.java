package com.example.authentification.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.authentification.dto.AuthResponse;
import com.example.authentification.dto.ChangePasswordRequest;
import com.example.authentification.dto.LoginRequest;
import com.example.authentification.dto.RegisterRequest;
import com.example.authentification.entity.AppUser;
import com.example.authentification.repository.AppUserRepository;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return new AuthResponse("Username already taken");
        }
        AppUser user = AppUser.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .roles(request.roles() == null || request.roles().isBlank() ? "ROLE_USER" : request.roles())
            .build();
        userRepository.save(user);
        return new AuthResponse("User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        if (authentication.isAuthenticated()) {
            return new AuthResponse("Login successful");
        }
        return new AuthResponse("Invalid credentials");
    }

    @Transactional
    public AuthResponse changePassword(String username, ChangePasswordRequest request) {
        // Validate passwords match
        if (!request.newPassword().equals(request.confirmPassword())) {
            return new AuthResponse("New passwords do not match");
        }

        // Validate current password
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.currentPassword())
            );
        } catch (Exception e) {
            return new AuthResponse("Current password is incorrect");
        }

        // Update password
        AppUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new AuthResponse("Password changed successfully");
    }
}
