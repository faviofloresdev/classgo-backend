package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.ActivityEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_activity_events")
public class StudentActivityEvent extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private StudentSession session;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private ActivityEventType eventType;
    @Column(name = "event_payload", columnDefinition = "text")
    private String eventPayload;
    @Column(name = "happened_at", nullable = false)
    private Instant happenedAt;

    public StudentSession getSession() {
        return this.session;
    }

    public ActivityEventType getEventType() {
        return this.eventType;
    }

    public String getEventPayload() {
        return this.eventPayload;
    }

    public Instant getHappenedAt() {
        return this.happenedAt;
    }

    public void setSession(final StudentSession session) {
        this.session = session;
    }

    public void setEventType(final ActivityEventType eventType) {
        this.eventType = eventType;
    }

    public void setEventPayload(final String eventPayload) {
        this.eventPayload = eventPayload;
    }

    public void setHappenedAt(final Instant happenedAt) {
        this.happenedAt = happenedAt;
    }
}
