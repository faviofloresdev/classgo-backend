package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.RevokedAccessToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, java.util.UUID> {
    boolean existsByTokenId(String tokenId);
    void deleteByExpiresAtBefore(Instant instant);
}
