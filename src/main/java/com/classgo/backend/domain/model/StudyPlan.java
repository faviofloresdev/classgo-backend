package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.ActivationMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "plans")
public class StudyPlan extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(length = 500)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    @Enumerated(EnumType.STRING)
    @Column(name = "activation_mode", nullable = false)
    private ActivationMode activationMode;
    @Column(name = "start_date")
    private LocalDate startDate;

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public User getTeacher() {
        return this.teacher;
    }

    public ActivationMode getActivationMode() {
        return this.activationMode;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setTeacher(final User teacher) {
        this.teacher = teacher;
    }

    public void setActivationMode(final ActivationMode activationMode) {
        this.activationMode = activationMode;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }
}
