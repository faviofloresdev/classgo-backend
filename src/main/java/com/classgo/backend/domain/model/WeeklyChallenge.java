package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.ChallengeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_challenges")
public class WeeklyChallenge extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private Classroom classroom;
    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    @Column(nullable = false)
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status = ChallengeStatus.DRAFT;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_teacher_id", nullable = false)
    private Teacher createdByTeacher;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "closed_at")
    private Instant closedAt;

    public Classroom getClassroom() {
        return this.classroom;
    }

    public Topic getTopic() {
        return this.topic;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public ChallengeStatus getStatus() {
        return this.status;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public Teacher getCreatedByTeacher() {
        return this.createdByTeacher;
    }

    public Instant getPublishedAt() {
        return this.publishedAt;
    }

    public Instant getClosedAt() {
        return this.closedAt;
    }

    public void setClassroom(final Classroom classroom) {
        this.classroom = classroom;
    }

    public void setTopic(final Topic topic) {
        this.topic = topic;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setStatus(final ChallengeStatus status) {
        this.status = status;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setCreatedByTeacher(final Teacher createdByTeacher) {
        this.createdByTeacher = createdByTeacher;
    }

    public void setPublishedAt(final Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setClosedAt(final Instant closedAt) {
        this.closedAt = closedAt;
    }
}
