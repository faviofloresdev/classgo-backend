package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.ActionResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AuthResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AuthUserResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.LoginRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.QuickStudentLoginRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateProfileRequest;
import com.classgo.backend.application.learning.LearningAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LearningAuthController {

    private final LearningAuthService authService;

    public LearningAuthController(LearningAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/student-quick-login")
    public AuthResponse quickStudentLogin(@Valid @RequestBody QuickStudentLoginRequest request) {
        return authService.quickStudentLogin(request);
    }

    @PostMapping("/auth/logout")
    public ActionResponse logout(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        authService.logout(authorizationHeader);
        return new ActionResponse("Logged out successfully");
    }

    @GetMapping("/auth/me")
    public AuthUserResponse me() {
        return authService.me();
    }

    @PatchMapping("/users/me")
    public AuthUserResponse updateProfile(@RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(request);
    }
}
