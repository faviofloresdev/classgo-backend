package com.classgo.backend.application.auth;

import com.classgo.backend.api.auth.dto.AuthDtos.AuthResponse;
import com.classgo.backend.api.auth.dto.AuthDtos.GoogleLoginRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.LoginRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.RefreshRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.RegisterRequest;
import com.classgo.backend.api.auth.dto.AuthDtos.UserProfileResponse;
import com.classgo.backend.domain.enums.AuthProvider;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.Parent;
import com.classgo.backend.domain.model.RefreshToken;
import com.classgo.backend.domain.model.Teacher;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.ParentRepository;
import com.classgo.backend.domain.repository.RefreshTokenRepository;
import com.classgo.backend.domain.repository.TeacherRepository;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.infrastructure.config.AppProperties;
import com.classgo.backend.infrastructure.security.JwtService;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.DuplicateResourceException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(
        UserRepository userRepository,
        TeacherRepository teacherRepository,
        ParentRepository parentRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AppProperties appProperties,
        AuthenticationManager authenticationManager,
        GoogleTokenVerifier googleTokenVerifier
    ) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
        this.authenticationManager = authenticationManager;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            throw new DuplicateResourceException("Email already registered");
        });
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setAuthProvider(AuthProvider.LOCAL);
        user = userRepository.save(user);
        createProfile(user, request.fullName());
        return buildAuthResponse(user, request.fullName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        return buildAuthResponse(user, resolveFullName(user));
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleUser googleUser = googleTokenVerifier.verify(request.idToken());
        User user = userRepository.findByEmailIgnoreCase(googleUser.email()).map(existing -> {
            if (existing.getRole() != request.role()) {
                throw new BusinessRuleViolationException("This Google account is already registered with a different role");
            }
            if (existing.getAuthProvider() == AuthProvider.LOCAL) {
                existing.setAuthProvider(AuthProvider.GOOGLE);
                existing = userRepository.save(existing);
            }
            return existing;
        }).orElseGet(() -> {
            User created = new User();
            created.setEmail(googleUser.email());
            created.setName(googleUser.name());
            created.setRole(request.role());
            created.setAuthProvider(AuthProvider.GOOGLE);
            created.setActive(true);
            created = userRepository.save(created);
            createProfile(created, googleUser.name());
            return created;
        });
        return buildAuthResponse(user, resolveFullName(user));
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token is invalid");
        }
        refreshToken.setRevokedAt(Instant.now());
        return buildAuthResponse(refreshToken.getUser(), resolveFullName(refreshToken.getUser()));
    }

    public UserProfileResponse me() {
        User user = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getRole(), resolveFullName(user));
    }

    private void createProfile(User user, String fullName) {
        if (user.getRole() == UserRole.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUser(user);
            teacher.setFullName(fullName);
            teacherRepository.save(teacher);
        } else {
            Parent parent = new Parent();
            parent.setUser(user);
            parent.setFullName(fullName);
            parentRepository.save(parent);
        }
    }

    private AuthResponse buildAuthResponse(User user, String fullName) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(appProperties.jwt().refreshTokenExpiration()));
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, refreshToken.getToken(), new UserProfileResponse(user.getId(), user.getEmail(), user.getRole(), fullName));
    }

    private String resolveFullName(User user) {
        return user.getRole() == UserRole.TEACHER
            ? teacherRepository.findByUserId(user.getId()).map(Teacher::getFullName).orElse("Teacher")
            : parentRepository.findByUserId(user.getId()).map(Parent::getFullName).orElse("Parent");
    }
}
