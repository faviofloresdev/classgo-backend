package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "user_achievements", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
public class UserAchievement extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    @Column(name = "progress_value", nullable = false)
    private long progressValue;
    @Column(nullable = false)
    private boolean unlocked;
    @Column(name = "unlocked_at")
    private Instant unlockedAt;

    public User getUser() {
        return this.user;
    }

    public Achievement getAchievement() {
        return this.achievement;
    }

    public long getProgressValue() {
        return this.progressValue;
    }

    public boolean isUnlocked() {
        return this.unlocked;
    }

    public Instant getUnlockedAt() {
        return this.unlockedAt;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setAchievement(final Achievement achievement) {
        this.achievement = achievement;
    }

    public void setProgressValue(final long progressValue) {
        this.progressValue = progressValue;
    }

    public void setUnlocked(final boolean unlocked) {
        this.unlocked = unlocked;
    }

    public void setUnlockedAt(final Instant unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}
