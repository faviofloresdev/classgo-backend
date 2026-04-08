package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "classes")
public class Classroom extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String grade;
    private String section;
    @Column(name = "academic_year")
    private String academicYear;

    public Teacher getTeacher() {
        return this.teacher;
    }

    public String getName() {
        return this.name;
    }

    public String getGrade() {
        return this.grade;
    }

    public String getSection() {
        return this.section;
    }

    public String getAcademicYear() {
        return this.academicYear;
    }

    public void setTeacher(final Teacher teacher) {
        this.teacher = teacher;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setGrade(final String grade) {
        this.grade = grade;
    }

    public void setSection(final String section) {
        this.section = section;
    }

    public void setAcademicYear(final String academicYear) {
        this.academicYear = academicYear;
    }
}
