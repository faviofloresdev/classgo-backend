package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_topics")
public class PlanTopic extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyPlan plan;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private LearningTopic topic;
    @Column(name = "week_number", nullable = false)
    private int weekNumber;
    @Column(name = "is_active", nullable = false)
    private boolean active;

    public StudyPlan getPlan() {
        return this.plan;
    }

    public LearningTopic getTopic() {
        return this.topic;
    }

    public int getWeekNumber() {
        return this.weekNumber;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setPlan(final StudyPlan plan) {
        this.plan = plan;
    }

    public void setTopic(final LearningTopic topic) {
        this.topic = topic;
    }

    public void setWeekNumber(final int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
