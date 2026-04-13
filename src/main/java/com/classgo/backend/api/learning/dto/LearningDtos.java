package com.classgo.backend.api.learning.dto;

import com.classgo.backend.domain.enums.ActivationMode;
import com.classgo.backend.domain.enums.AchievementActivityType;
import com.classgo.backend.domain.enums.AchievementCategory;
import com.classgo.backend.domain.enums.AchievementFeature;
import com.classgo.backend.domain.enums.AchievementSection;
import com.classgo.backend.domain.enums.NotificationType;
import com.classgo.backend.domain.enums.TopicDifficulty;
import com.classgo.backend.domain.enums.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class LearningDtos {
    private LearningDtos() {
    }

    public record LoginRequest(@Email String email, @NotBlank String password) {
    }

    public record QuickStudentLoginRequest(@NotBlank String name) {
    }

    public record AuthUserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        String avatarId,
        String studentAvatarId,
        String parentAvatarId,
        int accumulatedXp,
        AchievementUpdateResponse achievements
    ) {
    }

    public record AuthResponse(String token, AuthUserResponse user) {
    }

    public record ActionResponse(String message) {
    }

    public record UpdateProfileRequest(String name, String avatarId, String studentAvatarId, String parentAvatarId) {
    }

    public record AvatarResponse(String id, String name, String category, String url) {
    }

    public record CreateClassroomRequest(@NotBlank String name, @NotBlank String code, String description) {
    }

    public record UpdateClassroomRequest(String name, String code, String description, UUID activePlanId, Integer currentWeek) {
    }

    public record AssignPlanRequest(UUID planId) {
    }

    public record JoinClassroomRequest(@NotBlank String code) {
    }

    public record TrackSectionVisitRequest(@NotNull AchievementSection section) {
    }

    public record TrackFeatureUseRequest(@NotNull AchievementFeature feature) {
    }

    public record TrackActivityTypeRequest(@NotNull AchievementActivityType activityType) {
    }

    public record BasicUserResponse(UUID id, String name, String avatarId) {
    }

    public record TeacherResponse(UUID id, String name, String avatarId) {
    }

    public record TopicResponse(
        UUID id,
        String name,
        String description,
        String icon,
        String color,
        TopicDifficulty difficulty,
        JsonNode questions,
        UUID teacherId,
        Instant createdAt
    ) {
    }

    public record PlanTopicResponse(UUID id, UUID planId, UUID topicId, int weekNumber, boolean isActive, TopicSummaryResponse topic) {
    }

    public record TopicSummaryResponse(UUID id, String name, String color, JsonNode questions) {
    }

    public record PlanResponse(
        UUID id,
        String name,
        String description,
        UUID teacherId,
        ActivationMode activationMode,
        LocalDate startDate,
        Instant createdAt,
        List<PlanTopicResponse> topics
    ) {
    }

    public record ClassroomResponse(
        UUID id,
        String name,
        String code,
        String description,
        UUID teacherId,
        UUID activePlanId,
        int currentWeek,
        Instant createdAt
    ) {
    }

    public record ClassroomWithDetailsResponse(
        UUID id,
        String name,
        String code,
        String description,
        UUID teacherId,
        UUID activePlanId,
        int currentWeek,
        Instant createdAt,
        TeacherResponse teacher,
        List<BasicUserResponse> students,
        PlanResponse plan
    ) {
    }

    public record CreatePlanRequest(
        @NotBlank String name,
        String description,
        @NotNull ActivationMode activationMode,
        LocalDate startDate
    ) {
    }

    public record UpdatePlanRequest(String name, String description, ActivationMode activationMode, LocalDate startDate) {
    }

    public record CreateTopicRequest(
        @NotBlank String name,
        String description,
        String icon,
        String color,
        @NotNull TopicDifficulty difficulty,
        @NotNull JsonNode questions
    ) {
    }

    public record UpdateTopicRequest(String name, String description, String icon, String color, TopicDifficulty difficulty, JsonNode questions) {
    }

    public record AddPlanTopicRequest(UUID topicId, Integer weekNumber) {
    }

    public record ReorderPlanTopicsRequest(@Size(min = 1) List<UUID> orderedTopicIds) {
    }

    public record ActivateWeekRequest(int weekNumber) {
    }

    public record ActivateWeekResponse(UUID planId, int activeWeekNumber) {
    }

    public record GameplayContextResponse(
        GameplayClassroomResponse classroom,
        GameplayTopicResponse topic,
        boolean attemptAllowed,
        StudentResultResponse existingResult,
        List<InAppNotificationResponse> notifications
    ) {
    }

    public record GameplayClassroomResponse(UUID id, String name, int currentWeek) {
    }

    public record GameplayTopicResponse(UUID id, String name, String description, String color, JsonNode questions) {
    }

    public record InAppNotificationResponse(
        UUID id,
        NotificationType type,
        String message,
        JsonNode payload,
        Instant createdAt,
        Instant readAt
    ) {
    }

    public record ClassroomPresenceEventResponse(
        UUID classroomId,
        UUID studentId,
        String studentName,
        String avatarId,
        String message,
        Instant happenedAt
    ) {
    }

    public record PresenceStudentResponse(
        UUID studentId,
        String studentName,
        String avatarId,
        Instant lastSeenAt
    ) {
    }

    public record PresenceSnapshotResponse(
        UUID classroomId,
        List<PresenceStudentResponse> students,
        Instant happenedAt
    ) {
    }

    public record SubmitResultRequest(
        int weekNumber,
        int score,
        int timeSpent,
        int correctAnswers,
        int totalQuestions,
        @NotNull JsonNode answers
    ) {
    }

    public record StudentResultResponse(
        UUID id,
        UUID studentId,
        UUID classroomId,
        UUID topicId,
        int weekNumber,
        int score,
        Instant completedAt,
        int timeSpent,
        int correctAnswers,
        int totalQuestions,
        JsonNode answers,
        AchievementUpdateResponse achievements
    ) {
    }

    public record AchievementUnlockedResponse(
        String code,
        String name,
        AchievementCategory category,
        Instant unlockedAt
    ) {
    }

    public record AchievementProgressResponse(
        long completedChallenges,
        long currentWeeklyStreak,
        long highScoreChallenges,
        long perfectChallenges,
        long distinctSections,
        long distinctFeatures,
        long distinctActivityTypes,
        long firstCompletionCount
    ) {
    }

    public record AchievementUpdateResponse(
        List<AchievementUnlockedResponse> newlyUnlockedAchievements,
        AchievementProgressResponse updatedProgress
    ) {
    }

    public record StudentResultWithDetailsResponse(
        UUID id,
        UUID studentId,
        UUID classroomId,
        UUID topicId,
        int weekNumber,
        int score,
        Instant completedAt,
        int timeSpent,
        int correctAnswers,
        int totalQuestions,
        JsonNode answers,
        BasicUserResponse student,
        TopicLiteResponse topic
    ) {
    }

    public record TopicLiteResponse(UUID id, String name, String color) {
    }

    public record LeaderboardEntryResponse(BasicUserResponse student, int totalScore, int rank) {
    }

    public record HistoryEntryResponse(int weekNumber, UUID topicId, String topicName, int score, int timeSpent, Instant completedAt) {
    }

    public record TeacherClassroomDetailResponse(ClassroomWithDetailsResponse classroom, List<TeacherResultRowResponse> results) {
    }

    public record TeacherResultRowResponse(UUID studentId, int weekNumber, int score, int timeSpent, BasicUserResponse student) {
    }
}
