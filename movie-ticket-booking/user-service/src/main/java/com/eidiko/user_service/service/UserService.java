package com.eidiko.user_service.service;

import com.eidiko.user_service.dto.AuthRequest;
import com.eidiko.user_service.dto.AuthResponse;
import com.eidiko.user_service.dto.UserRequest;
import com.eidiko.user_service.entity.User;
import com.netflix.spectator.api.histogram.PercentileBuckets;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    User register(UserRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);
    Map<String, String> validateToken(String token);
    Optional<User> findByUsername(String username);
    User update(String username, UserRequest request);
    void delete(User user);

    List<User> findAll();
}
