package com.example.authentification.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank String newPassword,
    @NotBlank String confirmPassword
) {}
