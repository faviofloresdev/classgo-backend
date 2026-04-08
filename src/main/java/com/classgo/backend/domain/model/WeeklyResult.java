package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "weekly_results", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "challenge_id"}))
public class WeeklyResult extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private WeeklyChallenge challenge;
    @Column(name = "total_points", nullable = false)
    private int totalPoints;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal accuracy;
    @Column(name = "completed_activities", nullable = false)
    private int completedActivities;
    @Column(name = "active_seconds", nullable = false)
    private long activeSeconds;
    @Column(name = "rank_position", nullable = false)
    private int rankPosition;
    @Column(name = "strengths_summary")
    private String strengthsSummary;
    @Column(name = "weaknesses_summary")
    private String weaknessesSummary;
    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public Student getStudent() {
        return this.student;
    }

    public WeeklyChallenge getChallenge() {
        return this.challenge;
    }

    public int getTotalPoints() {
        return this.totalPoints;
    }

    public BigDecimal getAccuracy() {
        return this.accuracy;
    }

    public int getCompletedActivities() {
        return this.completedActivities;
    }

    public long getActiveSeconds() {
        return this.activeSeconds;
    }

    public int getRankPosition() {
        return this.rankPosition;
    }

    public String getStrengthsSummary() {
        return this.strengthsSummary;
    }

    public String getWeaknessesSummary() {
        return this.weaknessesSummary;
    }

    public Instant getGeneratedAt() {
        return this.generatedAt;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }

    public void setChallenge(final WeeklyChallenge challenge) {
        this.challenge = challenge;
    }

    public void setTotalPoints(final int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setAccuracy(final BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public void setCompletedActivities(final int completedActivities) {
        this.completedActivities = completedActivities;
    }

    public void setActiveSeconds(final long activeSeconds) {
        this.activeSeconds = activeSeconds;
    }

    public void setRankPosition(final int rankPosition) {
        this.rankPosition = rankPosition;
    }

    public void setStrengthsSummary(final String strengthsSummary) {
        this.strengthsSummary = strengthsSummary;
    }

    public void setWeaknessesSummary(final String weaknessesSummary) {
        this.weaknessesSummary = weaknessesSummary;
    }

    public void setGeneratedAt(final Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
