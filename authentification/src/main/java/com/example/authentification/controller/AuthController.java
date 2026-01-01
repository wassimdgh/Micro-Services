package com.example.authentification.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authentification.dto.AuthResponse;
import com.example.authentification.dto.ChangePasswordRequest;
import com.example.authentification.dto.LoginRequest;
import com.example.authentification.dto.RegisterRequest;
import com.example.authentification.entity.AppUser;
import com.example.authentification.repository.AppUserRepository;
import com.example.authentification.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository userRepository;

    public AuthController(AuthService authService, AppUserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Validated @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @Validated @RequestBody ChangePasswordRequest request,
            Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok(authService.changePassword(username, request));
    }
}
