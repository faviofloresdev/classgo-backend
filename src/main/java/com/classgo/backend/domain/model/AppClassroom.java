package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "classrooms")
public class AppClassroom extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true, length = 10)
    private String code;
    @Column(length = 500)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_plan_id")
    private StudyPlan activePlan;
    @Column(name = "current_week", nullable = false)
    private int currentWeek;

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public User getTeacher() {
        return this.teacher;
    }

    public StudyPlan getActivePlan() {
        return this.activePlan;
    }

    public int getCurrentWeek() {
        return this.currentWeek;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setTeacher(final User teacher) {
        this.teacher = teacher;
    }

    public void setActivePlan(final StudyPlan activePlan) {
        this.activePlan = activePlan;
    }

    public void setCurrentWeek(final int currentWeek) {
        this.currentWeek = currentWeek;
    }
}
