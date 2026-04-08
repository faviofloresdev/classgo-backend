package com.classgo.backend.api.auth;

import com.classgo.backend.api.auth.dto.AuthDtos.AuthResponse;
import com.classgo.backend.api.auth.dto.AuthDtos.GoogleLoginRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.LoginRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.RefreshRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.RegisterRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.UserProfileResponse;
import com.classgo.backend.application.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.googleLogin(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    public UserProfileResponse me() {
        return authService.me();
    }
}
