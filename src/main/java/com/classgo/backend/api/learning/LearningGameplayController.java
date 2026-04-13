package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.ActionResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.AchievementUpdateResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.GameplayContextResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.InAppNotificationResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultWithDetailsResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.TrackActivityTypeRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.TrackFeatureUseRequest;
import com.classgo.backend.api.learning.dto.LearningDtos.TrackSectionVisitRequest;
import com.classgo.backend.application.learning.LearningPlatformService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class LearningGameplayController {

    private final LearningPlatformService service;

    public LearningGameplayController(LearningPlatformService service) {
        this.service = service;
    }

    @GetMapping("/gameplay/context")
    public GameplayContextResponse gameplayContext(@RequestParam UUID classroomId) {
        return service.gameplayContext(classroomId);
    }

    @GetMapping(path = "/gameplay/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter classroomPresenceStream(@RequestParam UUID classroomId) {
        return service.subscribeToClassroomPresence(classroomId);
    }

    @PostMapping("/gameplay/presence/connect")
    public ActionResponse connectPresence(@RequestParam UUID classroomId) {
        service.connectClassroomPresence(classroomId);
        return new ActionResponse("Presence connected successfully");
    }

    @PostMapping("/gameplay/presence/heartbeat")
    public ActionResponse heartbeatPresence(@RequestParam UUID classroomId) {
        service.heartbeatClassroomPresence(classroomId);
        return new ActionResponse("Presence heartbeat recorded successfully");
    }

    @PostMapping("/gameplay/presence/disconnect")
    public ActionResponse disconnectPresence(@RequestParam UUID classroomId) {
        service.disconnectClassroomPresence(classroomId);
        return new ActionResponse("Presence disconnected successfully");
    }

    @GetMapping("/gameplay/notifications")
    public List<InAppNotificationResponse> classroomNotifications(@RequestParam UUID classroomId) {
        return service.classroomNotifications(classroomId);
    }

    @PatchMapping("/gameplay/notifications/{notificationId}/read")
    public ActionResponse markNotificationRead(@PathVariable UUID notificationId, @RequestParam UUID classroomId) {
        service.markNotificationRead(classroomId, notificationId);
        return new ActionResponse("Notification marked as read successfully");
    }

    @GetMapping("/students/me/results")
    public List<StudentResultWithDetailsResponse> studentResults(@RequestParam(required = false) UUID classroomId) {
        return service.studentResults(classroomId);
    }

    @PostMapping("/achievements/section-visits")
    public AchievementUpdateResponse trackSectionVisit(@Valid @RequestBody TrackSectionVisitRequest request) {
        return service.trackSectionVisit(request.section());
    }

    @PostMapping("/achievements/feature-uses")
    public AchievementUpdateResponse trackFeatureUse(@Valid @RequestBody TrackFeatureUseRequest request) {
        return service.trackFeatureUse(request.feature());
    }

    @PostMapping("/achievements/activity-types")
    public AchievementUpdateResponse trackActivityType(@Valid @RequestBody TrackActivityTypeRequest request) {
        return service.trackActivityTypeCompleted(request.activityType());
    }
}
