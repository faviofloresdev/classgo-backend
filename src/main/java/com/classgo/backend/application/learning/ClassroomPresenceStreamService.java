package com.classgo.backend.application.learning;

import com.classgo.backend.api.learning.dto.LearningDtos.ClassroomPresenceEventResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.PresenceSnapshotResponse;
import com.classgo.backend.api.learning.dto.LearningDtos.PresenceStudentResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ClassroomPresenceStreamService {

    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;
    private static final Duration PRESENCE_TTL = Duration.ofSeconds(45);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClassroomPresenceStreamService.class);

    private final Map<UUID, Map<UUID, CopyOnWriteArrayList<SseEmitter>>> classroomSubscribers = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, PresenceState>> classroomPresence = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID classroomId, UUID userId, String studentName, String avatarId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        classroomSubscribers
            .computeIfAbsent(classroomId, ignored -> new ConcurrentHashMap<>())
            .computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>())
            .add(emitter);
        classroomPresence
            .computeIfAbsent(classroomId, ignored -> new ConcurrentHashMap<>())
            .computeIfAbsent(userId, ignored -> new PresenceState(studentName, avatarId))
            .refreshProfile(studentName, avatarId);

        emitter.onCompletion(() -> removeEmitter(classroomId, userId, emitter));
        emitter.onTimeout(() -> removeEmitter(classroomId, userId, emitter));
        emitter.onError(ex -> removeEmitter(classroomId, userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                .name("subscribed")
                .data(Map.of("classroomId", classroomId, "status", "connected")));
            emitter.send(SseEmitter.event()
                .name("presence-snapshot")
                .data(buildSnapshot(classroomId, userId)));
        } catch (IOException ex) {
            removeEmitter(classroomId, userId, emitter);
        }
        return emitter;
    }

    public boolean connect(UUID classroomId, UUID studentId, String studentName, String avatarId) {
        PresenceState state = classroomPresence
            .computeIfAbsent(classroomId, ignored -> new ConcurrentHashMap<>())
            .computeIfAbsent(studentId, ignored -> new PresenceState(studentName, avatarId));
        synchronized (state) {
            state.refreshProfile(studentName, avatarId);
            state.connectionCount++;
            state.lastSeenAt = Instant.now();
            if (state.online) {
                return false;
            }
            state.online = true;
            return true;
        }
    }

    public void heartbeat(UUID classroomId, UUID studentId) {
        Map<UUID, PresenceState> classroomStates = classroomPresence.get(classroomId);
        if (classroomStates == null) {
            return;
        }
        PresenceState state = classroomStates.get(studentId);
        if (state == null) {
            return;
        }
        synchronized (state) {
            state.lastSeenAt = Instant.now();
        }
    }

    public boolean disconnect(UUID classroomId, UUID studentId) {
        Map<UUID, PresenceState> classroomStates = classroomPresence.get(classroomId);
        if (classroomStates == null) {
            return false;
        }
        PresenceState state = classroomStates.get(studentId);
        if (state == null) {
            return false;
        }
        synchronized (state) {
            if (state.connectionCount > 0) {
                state.connectionCount--;
            }
            state.lastSeenAt = Instant.now();
            if (state.connectionCount > 0 || !state.online) {
                return false;
            }
            state.online = false;
        }
        publishStudentDisconnected(classroomId, studentId);
        return true;
    }

    public void publishStudentConnected(UUID classroomId, UUID actorStudentId) {
        Map<UUID, PresenceState> classroomStates = classroomPresence.get(classroomId);
        if (classroomStates == null) {
            return;
        }
        PresenceState actor = classroomStates.get(actorStudentId);
        if (actor == null) {
            return;
        }
        ClassroomPresenceEventResponse payload;
        synchronized (actor) {
            payload = new ClassroomPresenceEventResponse(
                classroomId,
                actorStudentId,
                actor.studentName,
                actor.avatarId,
                actor.studentName + " se conecto al aula",
                actor.lastSeenAt
            );
        }
        List<UUID> recipientIds = onlineRecipientIds(classroomId, actorStudentId);
        publishPresenceEvent(classroomId, actorStudentId, recipientIds, payload, "student-connected");
    }

    public void expireInactiveConnections() {
        Instant now = Instant.now();
        List<DisconnectedPresence> expired = new ArrayList<>();
        for (Map.Entry<UUID, Map<UUID, PresenceState>> classroomEntry : classroomPresence.entrySet()) {
            UUID classroomId = classroomEntry.getKey();
            for (Map.Entry<UUID, PresenceState> studentEntry : classroomEntry.getValue().entrySet()) {
                UUID studentId = studentEntry.getKey();
                PresenceState state = studentEntry.getValue();
                boolean shouldDisconnect;
                synchronized (state) {
                    shouldDisconnect = state.online
                        && state.lastSeenAt != null
                        && Duration.between(state.lastSeenAt, now).compareTo(PRESENCE_TTL) > 0;
                    if (shouldDisconnect) {
                        state.online = false;
                        state.connectionCount = 0;
                    }
                }
                if (shouldDisconnect) {
                    expired.add(new DisconnectedPresence(classroomId, studentId));
                }
            }
        }
        expired.forEach(disconnected -> publishStudentDisconnected(disconnected.classroomId(), disconnected.studentId()));
    }

    public void publishKeepAlive() {
        Instant now = Instant.now();
        for (Map.Entry<UUID, Map<UUID, CopyOnWriteArrayList<SseEmitter>>> classroomEntry : classroomSubscribers.entrySet()) {
            UUID classroomId = classroomEntry.getKey();
            for (Map.Entry<UUID, CopyOnWriteArrayList<SseEmitter>> userEntry : classroomEntry.getValue().entrySet()) {
                UUID userId = userEntry.getKey();
                for (SseEmitter emitter : userEntry.getValue()) {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("ping")
                            .data(Map.of("classroomId", classroomId, "timestamp", now)));
                    } catch (IOException ex) {
                        removeEmitter(classroomId, userId, emitter);
                    }
                }
            }
        }
    }

    private void publishPresenceEvent(
        UUID classroomId,
        UUID actorStudentId,
        List<UUID> recipientIds,
        ClassroomPresenceEventResponse payload,
        String eventName
    ) {
        Map<UUID, CopyOnWriteArrayList<SseEmitter>> subscribers = classroomSubscribers.get(classroomId);
        if (subscribers == null || subscribers.isEmpty()) {
            log.info("No SSE subscribers available for classroom {} when notifying {} from {}", classroomId, eventName, actorStudentId);
            return;
        }
        int recipientsNotified = 0;
        for (UUID recipientId : recipientIds) {
            if (recipientId.equals(actorStudentId)) {
                continue;
            }
            List<SseEmitter> emitters = subscribers.get(recipientId);
            if (emitters == null || emitters.isEmpty()) {
                continue;
            }
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload));
                    recipientsNotified++;
                } catch (IOException ex) {
                    removeEmitter(classroomId, recipientId, emitter);
                }
            }
        }
        log.info(
            "Published frontend {} event for classroom {}. actorStudentId={}, recipientUsers={}, deliveredEmitters={}",
            eventName,
            classroomId,
            actorStudentId,
            recipientIds.size(),
            recipientsNotified
        );
    }

    private void removeEmitter(UUID classroomId, UUID userId, SseEmitter emitter) {
        Map<UUID, CopyOnWriteArrayList<SseEmitter>> classroomEmitters = classroomSubscribers.get(classroomId);
        if (classroomEmitters == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> userEmitters = classroomEmitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            classroomEmitters.remove(userId);
        }
        if (classroomEmitters.isEmpty()) {
            classroomSubscribers.remove(classroomId);
        }
    }

    private void publishStudentDisconnected(UUID classroomId, UUID actorStudentId) {
        Map<UUID, PresenceState> classroomStates = classroomPresence.get(classroomId);
        if (classroomStates == null) {
            return;
        }
        PresenceState actor = classroomStates.get(actorStudentId);
        if (actor == null) {
            return;
        }
        ClassroomPresenceEventResponse payload;
        synchronized (actor) {
            payload = new ClassroomPresenceEventResponse(
                classroomId,
                actorStudentId,
                actor.studentName,
                actor.avatarId,
                actor.studentName + " se desconecto del aula",
                Instant.now()
            );
        }
        List<UUID> recipientIds = onlineRecipientIds(classroomId, actorStudentId);
        publishPresenceEvent(classroomId, actorStudentId, recipientIds, payload, "student-disconnected");
    }

    private List<UUID> onlineRecipientIds(UUID classroomId, UUID actorStudentId) {
        Map<UUID, PresenceState> classroomStates = classroomPresence.get(classroomId);
        if (classroomStates == null || classroomStates.isEmpty()) {
            return List.of();
        }
        List<UUID> recipientIds = new ArrayList<>();
        for (Map.Entry<UUID, PresenceState> entry : classroomStates.entrySet()) {
            if (entry.getKey().equals(actorStudentId)) {
                continue;
            }
            PresenceState state = entry.getValue();
            synchronized (state) {
                if (state.online) {
                    recipientIds.add(entry.getKey());
                }
            }
        }
        return recipientIds;
    }

    private PresenceSnapshotResponse buildSnapshot(UUID classroomId, UUID excludedStudentId) {
        Map<UUID, PresenceState> classroomStates = classroomPresence.get(classroomId);
        if (classroomStates == null || classroomStates.isEmpty()) {
            return new PresenceSnapshotResponse(classroomId, List.of(), Instant.now());
        }
        List<PresenceStudentResponse> students = new ArrayList<>();
        for (Map.Entry<UUID, PresenceState> entry : classroomStates.entrySet()) {
            if (entry.getKey().equals(excludedStudentId)) {
                continue;
            }
            PresenceState state = entry.getValue();
            synchronized (state) {
                if (state.online) {
                    students.add(new PresenceStudentResponse(
                        entry.getKey(),
                        state.studentName,
                        state.avatarId,
                        state.lastSeenAt
                    ));
                }
            }
        }
        students.sort(Comparator.comparing(PresenceStudentResponse::studentName, String.CASE_INSENSITIVE_ORDER));
        return new PresenceSnapshotResponse(classroomId, students, Instant.now());
    }

    private static final class PresenceState {
        private String studentName;
        private String avatarId;
        private int connectionCount;
        private Instant lastSeenAt;
        private boolean online;

        private PresenceState(String studentName, String avatarId) {
            this.studentName = studentName;
            this.avatarId = avatarId;
            this.lastSeenAt = Instant.now();
        }

        private void refreshProfile(String studentName, String avatarId) {
            this.studentName = studentName;
            this.avatarId = avatarId;
        }
    }

    private record DisconnectedPresence(UUID classroomId, UUID studentId) {
    }
}
