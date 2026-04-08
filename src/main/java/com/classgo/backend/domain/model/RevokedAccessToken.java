package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "revoked_access_tokens")
public class RevokedAccessToken extends BaseEntity {
    @Column(name = "token_id", nullable = false, unique = true)
    private String tokenId;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public String getTokenId() {
        return this.tokenId;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public void setTokenId(final String tokenId) {
        this.tokenId = tokenId;
    }

    public void setExpiresAt(final Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
