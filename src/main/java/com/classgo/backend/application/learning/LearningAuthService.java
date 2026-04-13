package com.classgo.backend.application.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.AuthResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUpdateResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.LoginRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.QuickStudentLoginRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateProfileRequest;
import com.classgo.backend.application.achievements.AchievementEventService;
import com.classgo.backend.application.auth.AccessTokenRevocationService;
import com.classgo.backend.domain.enums.AuthProvider;
import com.classgo.backend.domain.enums.UserRole;
import com.classgo.backend.domain.model.StudentAttempt;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.StudentAttemptRepository;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.infrastructure.security.JwtService;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final AchievementEventService achievementEventService;
    private final StudentAttemptRepository studentAttemptRepository;

    public LearningAuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        LearningSupport support,
        AccessTokenRevocationService accessTokenRevocationService,
        AchievementEventService achievementEventService,
        StudentAttemptRepository studentAttemptRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.support = support;
        this.accessTokenRevocationService = accessTokenRevocationService;
        this.achievementEventService = achievementEventService;
        this.studentAttemptRepository = studentAttemptRepository;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        return new AuthResponse(jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole()), support.authUserResponse(user, accumulatedXp(user), null));
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
        user.setParentAvatarId("parent-1");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user = userRepository.save(user);
        return new AuthResponse(jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole()), support.authUserResponse(user, accumulatedXp(user), null));
    }

    public com.classgo.backend.api.learning.dto.LearningDtos.AuthUserResponse me() {
        User user = userRepository.findById(SecurityUtils.currentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        return support.authUserResponse(user, accumulatedXp(user), null);
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
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        String previousName = user.getName();
        String previousAvatarId = user.getAvatarId();
        String previousParentAvatarId = user.getParentAvatarId();
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        String requestedStudentAvatarId = request.studentAvatarId() != null ? request.studentAvatarId() : request.avatarId();
        if (requestedStudentAvatarId != null && !requestedStudentAvatarId.isBlank()) {
            user.setAvatarId(requestedStudentAvatarId.trim());
        }
        if (request.parentAvatarId() != null && !request.parentAvatarId().isBlank()) {
            user.setParentAvatarId(request.parentAvatarId().trim());
        }
        User saved = userRepository.save(user);
        AchievementUpdateResponse achievements = achievementEventService.onProfileUpdated(
            saved,
            previousName,
            previousAvatarId,
            previousParentAvatarId
        );
        return support.authUserResponse(saved, accumulatedXp(saved), achievements);
    }

    private int accumulatedXp(User user) {
        if (user.getRole() != UserRole.STUDENT) {
            return 0;
        }
        List<StudentAttempt> attempts = studentAttemptRepository.findAllByStudentIdOrderByCompletedAtDesc(user.getId());
        Map<String, StudentAttempt> bestByChallenge = new LinkedHashMap<>();
        for (StudentAttempt attempt : attempts) {
            String key = attempt.getClassroom().getId() + "|" + attempt.getTopic().getId() + "|" + attempt.getWeekNumber();
            StudentAttempt current = bestByChallenge.get(key);
            if (current == null || compareAttempts(attempt, current) > 0) {
                bestByChallenge.put(key, attempt);
            }
        }
        return bestByChallenge.values().stream().mapToInt(StudentAttempt::getScore).sum();
    }

    private int compareAttempts(StudentAttempt left, StudentAttempt right) {
        int byScore = Integer.compare(left.getScore(), right.getScore());
        if (byScore != 0) {
            return byScore;
        }
        int byTime = Integer.compare(right.getTimeSpent(), left.getTimeSpent());
        if (byTime != 0) {
            return byTime;
        }
        return right.getCompletedAt().compareTo(left.getCompletedAt());
    }
}
