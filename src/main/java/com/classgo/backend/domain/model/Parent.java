package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "parents")
public class Parent extends BaseEntity {
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "full_name", nullable = false)
    private String fullName;

    public User getUser() {
        return this.user;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }
}
