package com.eidiko.user_service.service;

import com.eidiko.user_service.dto.AuthRequest;
import com.eidiko.user_service.dto.AuthResponse;
import com.eidiko.user_service.dto.UserRequest;
import com.eidiko.user_service.dto.UserResponseDto;
import com.eidiko.user_service.entity.RefreshToken;
import com.eidiko.user_service.entity.User;
import com.eidiko.user_service.exception.UserAlreadyExistException;
import com.eidiko.user_service.exception.UserNotFoundException;
import com.eidiko.user_service.repository.UserRepository;
import com.eidiko.user_service.security.CustomUserDetailsService;
import com.eidiko.user_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public User register(UserRequest request) {
        log.info("UserRequest {}", request);
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("Username or email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole() != null ? request.getRole().trim().toUpperCase() : "USER");
        log.info("User {}", user);
        return userRepository.save(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) throws ExecutionException, InterruptedException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        log.info("Authentication successful ");

        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();
        CompletableFuture<String> accessTokenFuture =
                CompletableFuture.supplyAsync(() -> jwtUtil.generateAccessToken(username, user.getRole()));
        CompletableFuture<RefreshToken> refreshTokenFuture =
                CompletableFuture.supplyAsync(() -> refreshTokenService.createRefreshToken(username));
        String accessToken = null;
        RefreshToken refreshToken = null;
        accessToken = accessTokenFuture.get();
        refreshToken = refreshTokenFuture.get();
        if (refreshToken == null) {
            throw new IllegalArgumentException("Failed to create refresh token for user: " + username);
        }
        UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getPhoneNumber(), user.getRole());
        return new AuthResponse(accessToken, refreshToken.getToken(), userResponseDto);

    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshToken).orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(tokenEntity);
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
        UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getPhoneNumber(), user.getRole());

        return new AuthResponse(newAccessToken, refreshToken, userResponseDto);
    }

    @Override
    public Map<String, String> validateToken(String token) throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> validateTokenFuture = CompletableFuture.supplyAsync(() -> jwtUtil.validateToken(token));
        CompletableFuture<String> usernameFuture = CompletableFuture.supplyAsync(() -> jwtUtil.extractUsername(token));
        CompletableFuture<String> roleFuture = CompletableFuture.supplyAsync(() -> jwtUtil.extractRole(token));


        boolean isValid = validateTokenFuture.get();
        String username = usernameFuture.get();
        String role = roleFuture.get();

        log.info("inside validate api service");
        if (isValid) {
            log.info("validated successfully ");
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("role", role);
            log.info("response in service -{}", response.get("username"));
            return response;
        }
        log.info("validation failed ");
        throw new IllegalArgumentException("Invalid token");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User update(String username, UserRequest request) {
        return userRepository.findByUsername(username).map(existingUser -> {
            existingUser.setEmail(request.getEmail());
            existingUser.setFullName(request.getFullName());
            existingUser.setPhoneNumber(request.getPhoneNumber());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setUsername(request.getUsername());
            existingUser.setRole(request.getRole().trim().toUpperCase());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new UserNotFoundException("UserNot Found "));

    }

    @Override
    public void delete(User user) {
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findByIsActiveTrue();
    }


}
