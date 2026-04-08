package com.classgo.backend.api.results.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ResultDtos {
    private ResultDtos() {}

    public record ProgressResponse(int points, BigDecimal accuracy, long answered, long totalQuestions, long completedActivities, long activeSeconds) {}
    public record LeaderboardResponse(UUID challengeId, List<LeaderboardEntry> entries, Integer currentStudentRank) {}
    public record LeaderboardEntry(int rank, String displayName, String avatarId, int points) {}
    public record WeeklyResultResponse(UUID studentId, UUID challengeId, int totalPoints, BigDecimal accuracy, int rankPosition,
                                       int completedActivities, long activeSeconds, String strengthsSummary, String weaknessesSummary) {}
}
