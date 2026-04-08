package com.classgo.backend.application.auth;

import com.classgo.backend.domain.model.RevokedAccessToken;
import com.classgo.backend.domain.repository.RevokedAccessTokenRepository;
import com.classgo.backend.infrastructure.security.JwtService;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessTokenRevocationService {

    private final JwtService jwtService;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;

    public AccessTokenRevocationService(
        JwtService jwtService,
        RevokedAccessTokenRepository revokedAccessTokenRepository
    ) {
        this.jwtService = jwtService;
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
    }

    @Transactional
    public void revoke(String token) {
        String tokenId = jwtService.extractTokenId(token);
        if (tokenId == null || revokedAccessTokenRepository.existsByTokenId(tokenId)) {
            return;
        }
        RevokedAccessToken revoked = new RevokedAccessToken();
        revoked.setTokenId(tokenId);
        revoked.setExpiresAt(jwtService.extractExpiration(token));
        revokedAccessTokenRepository.save(revoked);
    }

    public boolean isRevoked(String token) {
        String tokenId = jwtService.extractTokenId(token);
        return tokenId != null && revokedAccessTokenRepository.existsByTokenId(tokenId);
    }

    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void cleanupExpiredRevocations() {
        revokedAccessTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
