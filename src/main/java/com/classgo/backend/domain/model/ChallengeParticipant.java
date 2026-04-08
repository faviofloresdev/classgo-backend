package com.classgo.backend.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "challenge_participants", uniqueConstraints = @UniqueConstraint(columnNames = {"challenge_id", "student_id"}))
public class ChallengeParticipant extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private WeeklyChallenge challenge;
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public WeeklyChallenge getChallenge() {
        return this.challenge;
    }

    public Student getStudent() {
        return this.student;
    }

    public void setChallenge(final WeeklyChallenge challenge) {
        this.challenge = challenge;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }
}
