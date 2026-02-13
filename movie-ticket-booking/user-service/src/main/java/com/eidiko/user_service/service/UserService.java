package com.eidiko.user_service.service;

import com.eidiko.user_service.dto.AuthRequest;
import com.eidiko.user_service.dto.AuthResponse;
import com.eidiko.user_service.dto.UserRequest;
import com.eidiko.user_service.dto.UserResponseDto;
import com.eidiko.user_service.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface UserService {
    UserResponseDto register(UserRequest request);
    AuthResponse login(AuthRequest request) throws ExecutionException, InterruptedException;
    AuthResponse refreshToken(String refreshToken);
    Map<String, String> validateToken(String token) throws ExecutionException, InterruptedException;
    Optional<User> findByUsername(String username);
    User update(String username, UserRequest request);
    void delete(User user);

    List<User> findAll();
}
