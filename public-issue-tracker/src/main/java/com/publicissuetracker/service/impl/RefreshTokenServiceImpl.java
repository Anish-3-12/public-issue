package com.publicissuetracker.service.impl;

import com.publicissuetracker.model.RefreshToken;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.RefreshTokenRepository;
import com.publicissuetracker.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   @Value("${jwt.refreshExpiration}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        // generate a secure random token (base64)
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant expiresAt = Instant.now().plusMillis(refreshExpirationMs);
        RefreshToken rt = new RefreshToken(token, user, expiresAt);
        return refreshTokenRepository.save(rt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt() != null && rt.getExpiresAt().isAfter(Instant.now()));
    }

    @Override
    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllForUser(User user) {
        refreshTokenRepository.findByUser(user)
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }
}
