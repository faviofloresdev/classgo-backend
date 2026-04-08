package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "parent_student_links", uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id", "student_id"}))
public class ParentStudentLink extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @Column(name = "display_name", nullable = false)
    private String displayName;
    @Column(name = "avatar_id")
    private String avatarId;
    private String nickname;

    public Parent getParent() {
        return this.parent;
    }

    public Student getStudent() {
        return this.student;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getAvatarId() {
        return this.avatarId;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setParent(final Parent parent) {
        this.parent = parent;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setAvatarId(final String avatarId) {
        this.avatarId = avatarId;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }
}
