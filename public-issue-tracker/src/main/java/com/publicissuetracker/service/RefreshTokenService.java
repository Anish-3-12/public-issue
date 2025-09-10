package com.publicissuetracker.service;

import com.publicissuetracker.model.RefreshToken;
import com.publicissuetracker.model.User;

import java.util.Optional;

public interface RefreshTokenService {

    /**
     * Create and persist a new refresh token for the given user.
     * Returns the created RefreshToken entity.
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Validate a refresh token string. Returns the RefreshToken if valid AND not revoked/expired.
     */
    Optional<RefreshToken> findValidByToken(String token);

    /**
     * Revoke the given refresh token (mark revoked).
     */
    void revoke(RefreshToken refreshToken);

    /**
     * Revoke all refresh tokens for a user (logout everywhere).
     */
    void revokeAllForUser(User user);
}
