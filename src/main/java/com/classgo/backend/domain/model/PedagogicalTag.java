package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pedagogical_tags", uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "slug"}))
public class PedagogicalTag extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String slug;

    public User getTeacher() {
        return this.teacher;
    }

    public String getName() {
        return this.name;
    }

    public String getSlug() {
        return this.slug;
    }

    public void setTeacher(final User teacher) {
        this.teacher = teacher;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }
}
