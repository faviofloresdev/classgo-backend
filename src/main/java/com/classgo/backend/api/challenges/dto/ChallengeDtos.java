package com.classgo.backend.api.challenges.dto;

import com.classgo.backend.domain.enums.ChallengeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ChallengeDtos {
    private ChallengeDtos() {}

    public record CreateChallengeRequest(@NotNull UUID classId, @NotNull UUID topicId, @NotBlank String title, String description,
                                         @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
    public record ChallengeResponse(UUID id, UUID classId, UUID topicId, String title, String description, ChallengeStatus status,
                                    LocalDate startDate, LocalDate endDate, Instant publishedAt, Instant closedAt) {}
}
