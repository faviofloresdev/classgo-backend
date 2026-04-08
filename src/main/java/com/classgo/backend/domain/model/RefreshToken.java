package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true, length = 200)
    private String token;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;

    public User getUser() {
        return this.user;
    }

    public String getToken() {
        return this.token;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public Instant getRevokedAt() {
        return this.revokedAt;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setRevokedAt(final Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
