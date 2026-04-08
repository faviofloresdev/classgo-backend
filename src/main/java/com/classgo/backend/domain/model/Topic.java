package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "topics")
public class Topic extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    @Column(nullable = false)
    private String grade;
    @Column(nullable = false)
    private String title;
    private String description;

    public Subject getSubject() {
        return this.subject;
    }

    public String getGrade() {
        return this.grade;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setSubject(final Subject subject) {
        this.subject = subject;
    }

    public void setGrade(final String grade) {
        this.grade = grade;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
