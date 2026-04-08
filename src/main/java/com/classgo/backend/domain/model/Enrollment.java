package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private AppClassroom classroom;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public AppClassroom getClassroom() {
        return this.classroom;
    }

    public User getStudent() {
        return this.student;
    }

    public Instant getJoinedAt() {
        return this.joinedAt;
    }

    public void setClassroom(final AppClassroom classroom) {
        this.classroom = classroom;
    }

    public void setStudent(final User student) {
        this.student = student;
    }

    public void setJoinedAt(final Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
