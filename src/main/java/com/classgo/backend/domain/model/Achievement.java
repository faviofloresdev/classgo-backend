package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.AchievementAction;
import com.classgo.backend.domain.enums.AchievementActor;
import com.classgo.backend.domain.enums.AchievementCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "achievements")
public class Achievement extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AchievementCategory category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AchievementAction action;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AchievementActor actor;
    @Column
    private Integer threshold;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;
    @Column(name = "is_manual", nullable = false)
    private boolean manual;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public AchievementCategory getCategory() {
        return this.category;
    }

    public AchievementAction getAction() {
        return this.action;
    }

    public AchievementActor getActor() {
        return this.actor;
    }

    public Integer getThreshold() {
        return this.threshold;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isManual() {
        return this.manual;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCategory(final AchievementCategory category) {
        this.category = category;
    }

    public void setAction(final AchievementAction action) {
        this.action = action;
    }

    public void setActor(final AchievementActor actor) {
        this.actor = actor;
    }

    public void setThreshold(final Integer threshold) {
        this.threshold = threshold;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public void setManual(final boolean manual) {
        this.manual = manual;
    }

    public void setSortOrder(final int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
