package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.AchievementFactType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "achievement_global_facts", uniqueConstraints = @UniqueConstraint(columnNames = {"fact_type", "fact_key"}))
public class AchievementGlobalFact extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "fact_type", nullable = false, length = 50)
    private AchievementFactType factType;
    @Column(name = "fact_key", nullable = false, length = 200)
    private String factKey;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public AchievementFactType getFactType() {
        return this.factType;
    }

    public String getFactKey() {
        return this.factKey;
    }

    public User getUser() {
        return this.user;
    }

    public void setFactType(final AchievementFactType factType) {
        this.factType = factType;
    }

    public void setFactKey(final String factKey) {
        this.factKey = factKey;
    }

    public void setUser(final User user) {
        this.user = user;
    }
}
