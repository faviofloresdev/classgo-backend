package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private Classroom classroom;
    @Column(name = "student_code", nullable = false, unique = true)
    private String studentCode;
    @Column(name = "internal_alias")
    private String internalAlias;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Classroom getClassroom() {
        return this.classroom;
    }

    public String getStudentCode() {
        return this.studentCode;
    }

    public String getInternalAlias() {
        return this.internalAlias;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setClassroom(final Classroom classroom) {
        this.classroom = classroom;
    }

    public void setStudentCode(final String studentCode) {
        this.studentCode = studentCode;
    }

    public void setInternalAlias(final String internalAlias) {
        this.internalAlias = internalAlias;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
