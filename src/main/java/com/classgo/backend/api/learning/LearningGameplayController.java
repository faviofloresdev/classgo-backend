package com.classgo.backend.api.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.GameplayContextResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.InAppNotificationResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.StudentResultWithDetailsResponse;
import com.classgo.backend.application.learning.LearningPlatformService;
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
    public void connectPresence(@RequestParam UUID classroomId) {
        service.connectClassroomPresence(classroomId);
    }

    @PostMapping("/gameplay/presence/heartbeat")
    public void heartbeatPresence(@RequestParam UUID classroomId) {
        service.heartbeatClassroomPresence(classroomId);
    }

    @PostMapping("/gameplay/presence/disconnect")
    public void disconnectPresence(@RequestParam UUID classroomId) {
        service.disconnectClassroomPresence(classroomId);
    }

    @GetMapping("/gameplay/notifications")
    public List<InAppNotificationResponse> classroomNotifications(@RequestParam UUID classroomId) {
        return service.classroomNotifications(classroomId);
    }

    @PatchMapping("/gameplay/notifications/{notificationId}/read")
    public void markNotificationRead(@PathVariable UUID notificationId, @RequestParam UUID classroomId) {
        service.markNotificationRead(classroomId, notificationId);
    }

    @GetMapping("/students/me/results")
    public List<StudentResultWithDetailsResponse> studentResults(@RequestParam(required = false) UUID classroomId) {
        return service.studentResults(classroomId);
    }
}
