package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.AssignPlanRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.BasicUserResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.CreateClassroomRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.HistoryEntryResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.JoinClassroomRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.LeaderboardEntryResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.SubmitResultRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.TeacherClassroomDetailResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.UpdateClassroomRequest;
import com.classgo.backend.application.learning.LearningPlatformService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classrooms")
public class LearningClassroomController {

    private final LearningPlatformService service;

    public LearningClassroomController(LearningPlatformService service) {
        this.service = service;
    }

    @PostMapping
    public ClassroomResponse createClassroom(@Valid @RequestBody CreateClassroomRequest request) {
        return service.createClassroom(request);
    }

    @PatchMapping("/{classroomId}")
    public ClassroomResponse updateClassroom(@PathVariable UUID classroomId, @RequestBody UpdateClassroomRequest request) {
        return service.updateClassroom(classroomId, request);
    }

    @DeleteMapping("/{classroomId}")
    public void deleteClassroom(@PathVariable UUID classroomId) {
        service.deleteClassroom(classroomId);
    }

    @GetMapping("/{classroomId}")
    public ClassroomWithDetailsResponse classroom(@PathVariable UUID classroomId) {
        return service.classroom(classroomId);
    }

    @PostMapping("/{classroomId}/assign-plan")
    public ClassroomWithDetailsResponse assignPlan(@PathVariable UUID classroomId, @RequestBody AssignPlanRequest request) {
        return service.assignPlan(classroomId, request);
    }

    @PostMapping("/join")
    public ClassroomWithDetailsResponse joinClassroom(@Valid @RequestBody JoinClassroomRequest request) {
        return service.joinClassroom(request);
    }

    @PostMapping("/{classroomId}/enrollments")
    public void enrollStudent(@PathVariable UUID classroomId, @RequestBody Map<String, UUID> body) {
        service.enrollStudent(classroomId, body.get("studentId"));
    }

    @DeleteMapping("/{classroomId}/enrollments/{studentId}")
    public void removeEnrollment(@PathVariable UUID classroomId, @PathVariable UUID studentId) {
        service.removeEnrollment(classroomId, studentId);
    }

    @GetMapping("/{classroomId}/students")
    public List<BasicUserResponse> classroomStudents(@PathVariable UUID classroomId) {
        return service.classroomStudents(classroomId);
    }

    @PostMapping("/{classroomId}/topics/{topicId}/results")
    public StudentResultResponse submitResult(
        @PathVariable UUID classroomId,
        @PathVariable UUID topicId,
        @Valid @RequestBody SubmitResultRequest request
    ) {
        return service.submitResult(classroomId, topicId, request);
    }

    @GetMapping("/{classroomId}/results")
    public List<StudentResultWithDetailsResponse> classroomResults(
        @PathVariable UUID classroomId,
        @RequestParam(required = false) Integer weekNumber
    ) {
        return service.classroomResults(classroomId, weekNumber);
    }

    @GetMapping("/{classroomId}/leaderboard")
    public List<LeaderboardEntryResponse> leaderboard(
        @PathVariable UUID classroomId,
        @RequestParam(required = false) Integer weekNumber
    ) {
        return service.leaderboard(classroomId, weekNumber);
    }

    @GetMapping("/{classroomId}/students/{studentId}/history")
    public List<HistoryEntryResponse> studentHistory(@PathVariable UUID classroomId, @PathVariable UUID studentId) {
        return service.studentHistory(classroomId, studentId);
    }

    @GetMapping("/teachers/me")
    public List<ClassroomWithDetailsResponse> teacherClassrooms() {
        return service.teacherClassrooms();
    }

    @GetMapping("/teachers/me/{classroomId}/detail")
    public TeacherClassroomDetailResponse teacherClassroomDetail(@PathVariable UUID classroomId) {
        return service.teacherClassroomDetail(classroomId);
    }

    @GetMapping("/students/me")
    public List<ClassroomWithDetailsResponse> studentClassrooms() {
        return service.studentClassrooms();
    }
}
