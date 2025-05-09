package com.eidiko.user_service.service;

import com.eidiko.user_service.entity.RefreshToken;
import com.eidiko.user_service.entity.User;
import com.eidiko.user_service.exception.UserNotFoundException;
import com.eidiko.user_service.repository.RefreshTokenRepository;
import com.eidiko.user_service.repository.UserRepository;
import com.eidiko.user_service.security.CustomUserDetailsService;
import com.eidiko.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public RefreshToken createRefreshToken(String username) {
        User user = (User) customUserDetailsService.loadUserByUsername(username);
        log.info("User in createRefreshToken {}",user.getUsername());

        RefreshToken byUserId = refreshTokenRepository.findByUserId(user.getId());
        if(byUserId !=null) {
            refreshTokenRepository.delete(byUserId);
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshToken(user.getUsername()));
        refreshToken.setExpiryDate(jwtUtil.getExpirationDateFromToken(refreshToken.getToken()));
        log.info("refreshToken in createRefreshToken {}" ,refreshToken);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token has expired");
        }
    }
}
