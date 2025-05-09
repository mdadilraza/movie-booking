package com.eidiko.user_service.controller;

import com.eidiko.user_service.entity.RefreshToken;
import com.eidiko.user_service.repository.RefreshTokenRepository;
import com.eidiko.user_service.repository.UserRepository;
import com.eidiko.user_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/refresh")
public class RefreshTokenController {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/deleteByUser_Id/{userId}")
    public ResponseEntity<?> deleteByUser_Id(@PathVariable long userId){
        refreshTokenRepository.deleteByUser_Id(userId);
        return ResponseEntity.ok("deleted ");
    }
    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/deleteByUserId/{userId}")
    public ResponseEntity<?> deleteByUserId(@PathVariable long userId){
        refreshTokenRepository.deleteByUserId(userId);
        return ResponseEntity.ok("deleted ");
    }

    @DeleteMapping("/deleteByUserIdByQuery/{userId}")
    public ResponseEntity<?> deleteByUserIdByQuery(@PathVariable long userId){
        refreshTokenRepository.deleteByUserIdByQuery(userId);
        return ResponseEntity.ok("deleted ");
    }

    @GetMapping("/findByUser_Id/{userId}")
    public ResponseEntity<RefreshToken> findByUser_Id(@PathVariable long userId){
        return ResponseEntity.ok(refreshTokenRepository.findByUser_Id(userId));
    }


    @GetMapping("/existsByUsername/{username}")
    public boolean existsByUsername(@PathVariable String username){
        return userRepository.existsByUsername(username);
    }

}
