package com.classgo.backend.api.gameplay.dto;

import com.classgo.backend.api.results.dto.ResultDtos.ProgressResponse;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class GameplayDtos {
    private GameplayDtos() {}

    public record StartSessionRequest(@NotNull UUID studentId, @NotNull UUID challengeId) {}
    public record HeartbeatRequest(@NotNull Instant timestamp) {}
    public record SubmitAnswerRequest(@NotNull UUID sessionId, @NotNull UUID studentId, @NotNull UUID challengeId,
                                      @NotNull UUID questionId, UUID selectedOptionId, String answerText) {}
    public record EndSessionRequest(@NotNull Instant timestamp) {}
    public record SessionResponse(UUID sessionId, String status, Instant startedAt) {}
    public record SessionSummaryResponse(UUID sessionId, String status, long totalSeconds, long activeSeconds, Summary summary) {
        public record Summary(long answered, long correct, int points) {}
    }
    public record SubmitAnswerResponse(boolean correct, int pointsEarned, UUID correctOptionId, String explanation, int streak,
                                       ProgressResponse progress) {}
}
