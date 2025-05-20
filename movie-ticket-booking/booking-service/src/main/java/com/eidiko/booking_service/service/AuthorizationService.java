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
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        System.out.println("Authorities: ");
        auth.getAuthorities().forEach(aut -> System.out.println(aut.getAuthority()));
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }



}
