package com.classgo.backend.api.auth.dto;

import com.classgo.backend.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(@Email String email, @NotBlank String password, @NotNull UserRole role, @NotBlank String fullName) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record GoogleLoginRequest(@NotBlank String idToken, @NotNull UserRole role) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken, UserProfileResponse user) {}
    public record UserProfileResponse(UUID userId, String email, UserRole role, String fullName) {}
}
