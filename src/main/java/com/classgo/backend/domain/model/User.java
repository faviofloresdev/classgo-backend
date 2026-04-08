package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.AuthProvider;
import com.classgo.backend.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column(name = "avatar_id")
    private String avatarId;
    @Column(name = "password_hash")
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String getAvatarId() {
        return this.avatarId;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public UserRole getRole() {
        return this.role;
    }

    public AuthProvider getAuthProvider() {
        return this.authProvider;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAvatarId(final String avatarId) {
        this.avatarId = avatarId;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(final UserRole role) {
        this.role = role;
    }

    public void setAuthProvider(final AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
