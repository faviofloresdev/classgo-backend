package com.classgo.backend.application.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.AuthResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.LoginRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.QuickStudentLoginRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateProfileRequest;
import com.classgo.backend.application.auth.AccessTokenRevocationService;
import com.classgo.backend.domain.enums.AuthProvider;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.infrastructure.security.JwtService;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LearningSupport support;
    private final AccessTokenRevocationService accessTokenRevocationService;

    public LearningAuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        LearningSupport support,
        AccessTokenRevocationService accessTokenRevocationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.support = support;
        this.accessTokenRevocationService = accessTokenRevocationService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        return new AuthResponse(jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole()), support.authUserResponse(user));
    }

    @Transactional
    public AuthResponse quickStudentLogin(QuickStudentLoginRequest request) {
        User user = new User();
        user.setEmail("student-" + UUID.randomUUID() + "@quick.classgo.local");
        user.setName(request.name().trim());
        user.setRole(UserRole.STUDENT);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
        user.setAvatarId("animal-1");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user = userRepository.save(user);
        return new AuthResponse(jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole()), support.authUserResponse(user));
    }

    public com.classgo.backend.api.learning.dto.LearningDtos.AuthUserResponse me() {
        User user = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuario no encontrado"));
        return support.authUserResponse(user);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        accessTokenRevocationService.revoke(authorizationHeader.substring(7));
    }

    @Transactional
    public com.classgo.backend.api.learning.dto.LearningDtos.AuthUserResponse updateProfile(UpdateProfileRequest request) {
        User user = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuario no encontrado"));
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.avatarId() != null && !request.avatarId().isBlank()) {
            user.setAvatarId(request.avatarId().trim());
        }
        return support.authUserResponse(userRepository.save(user));
    }
}
