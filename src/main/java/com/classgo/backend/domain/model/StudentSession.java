package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_sessions")
public class StudentSession extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private WeeklyChallenge challenge;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "ended_at")
    private Instant endedAt;
    @Column(name = "last_activity_at")
    private Instant lastActivityAt;
    @Column(name = "total_seconds", nullable = false)
    private long totalSeconds;
    @Column(name = "active_seconds", nullable = false)
    private long activeSeconds;

    public Student getStudent() {
        return this.student;
    }

    public WeeklyChallenge getChallenge() {
        return this.challenge;
    }

    public SessionStatus getStatus() {
        return this.status;
    }

    public Instant getStartedAt() {
        return this.startedAt;
    }

    public Instant getEndedAt() {
        return this.endedAt;
    }

    public Instant getLastActivityAt() {
        return this.lastActivityAt;
    }

    public long getTotalSeconds() {
        return this.totalSeconds;
    }

    public long getActiveSeconds() {
        return this.activeSeconds;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }

    public void setChallenge(final WeeklyChallenge challenge) {
        this.challenge = challenge;
    }

    public void setStatus(final SessionStatus status) {
        this.status = status;
    }

    public void setStartedAt(final Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setEndedAt(final Instant endedAt) {
        this.endedAt = endedAt;
    }

    public void setLastActivityAt(final Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public void setTotalSeconds(final long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public void setActiveSeconds(final long activeSeconds) {
        this.activeSeconds = activeSeconds;
    }
}
