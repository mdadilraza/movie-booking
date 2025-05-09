package com.eidiko.user_service.service;

import com.eidiko.user_service.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);
    Optional<RefreshToken> findByToken(String token);
    void verifyExpiration(RefreshToken token);
}
