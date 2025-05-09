package com.eidiko.booking_service.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public Long getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName() != null ?
                Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()) : null;
    }

    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
