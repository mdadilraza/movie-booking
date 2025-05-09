package com.eidiko.user_service.controller;

import com.eidiko.user_service.dto.UserRequest;
import com.eidiko.user_service.entity.User;
import com.eidiko.user_service.repository.RefreshTokenRepository;
import com.eidiko.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getByAllUser() {
        return ResponseEntity.ok(userService.findAll());

    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String username, @RequestBody UserRequest request) {
        User update = userService.update(username, request);
        return ResponseEntity.ok(update);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    userService.delete(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
