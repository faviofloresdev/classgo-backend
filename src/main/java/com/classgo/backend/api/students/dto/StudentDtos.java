package com.classgo.backend.api.students.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class StudentDtos {
    private StudentDtos() {}

    public record JoinStudentRequest(@NotBlank String studentCode, @NotBlank String displayName, String avatarId) {}
    public record UpdateChildRequest(@NotBlank String displayName, String avatarId, String nickname) {}
    public record ChildResponse(UUID studentId, UUID classId, String className, String displayName, String avatarId, String nickname) {}
}
