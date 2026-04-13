package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_achievement_metrics", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserAchievementMetric extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "completed_challenges_count", nullable = false)
    private long completedChallengesCount;
    @Column(name = "current_weekly_streak", nullable = false)
    private long currentWeeklyStreak;
    @Column(name = "high_score_challenges_count", nullable = false)
    private long highScoreChallengesCount;
    @Column(name = "perfect_score_challenges_count", nullable = false)
    private long perfectScoreChallengesCount;
    @Column(name = "distinct_sections_count", nullable = false)
    private long distinctSectionsCount;
    @Column(name = "distinct_features_count", nullable = false)
    private long distinctFeaturesCount;
    @Column(name = "distinct_activity_types_count", nullable = false)
    private long distinctActivityTypesCount;
    @Column(name = "first_completion_count", nullable = false)
    private long firstCompletionCount;

    public User getUser() {
        return this.user;
    }

    public long getCompletedChallengesCount() {
        return this.completedChallengesCount;
    }

    public long getCurrentWeeklyStreak() {
        return this.currentWeeklyStreak;
    }

    public long getHighScoreChallengesCount() {
        return this.highScoreChallengesCount;
    }

    public long getPerfectScoreChallengesCount() {
        return this.perfectScoreChallengesCount;
    }

    public long getDistinctSectionsCount() {
        return this.distinctSectionsCount;
    }

    public long getDistinctFeaturesCount() {
        return this.distinctFeaturesCount;
    }

    public long getDistinctActivityTypesCount() {
        return this.distinctActivityTypesCount;
    }

    public long getFirstCompletionCount() {
        return this.firstCompletionCount;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setCompletedChallengesCount(final long completedChallengesCount) {
        this.completedChallengesCount = completedChallengesCount;
    }

    public void setCurrentWeeklyStreak(final long currentWeeklyStreak) {
        this.currentWeeklyStreak = currentWeeklyStreak;
    }

    public void setHighScoreChallengesCount(final long highScoreChallengesCount) {
        this.highScoreChallengesCount = highScoreChallengesCount;
    }

    public void setPerfectScoreChallengesCount(final long perfectScoreChallengesCount) {
        this.perfectScoreChallengesCount = perfectScoreChallengesCount;
    }

    public void setDistinctSectionsCount(final long distinctSectionsCount) {
        this.distinctSectionsCount = distinctSectionsCount;
    }

    public void setDistinctFeaturesCount(final long distinctFeaturesCount) {
        this.distinctFeaturesCount = distinctFeaturesCount;
    }

    public void setDistinctActivityTypesCount(final long distinctActivityTypesCount) {
        this.distinctActivityTypesCount = distinctActivityTypesCount;
    }

    public void setFirstCompletionCount(final long firstCompletionCount) {
        this.firstCompletionCount = firstCompletionCount;
    }
}
