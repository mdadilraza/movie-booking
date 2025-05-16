package com.eidiko.booking_service.controller;

import com.eidiko.booking_service.dto.SeatRequest;
import com.eidiko.booking_service.entity.Seats;
import com.eidiko.booking_service.service.AuthorizationService;
import com.eidiko.booking_service.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/seats")
@Slf4j
public class SeatController {
    private final SeatService seatsService;
    private final AuthorizationService authService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Seats> createSeat(@Valid @RequestBody SeatRequest request) {
        log.debug("Received request to create seat type: {}", request.getTypeName());
        if (!authService.isAdmin()) {
            throw new IllegalStateException("Only admins can create seat types");
        }
        Seats seat = seatsService.createSeat(request);
        return ResponseEntity.ok(seat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Seats> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        log.debug("Received request to update seat type with ID: {}", id);
        if (!authService.isAdmin()) {
            throw new IllegalStateException("Only admins can update seat types");
        }
        Seats seat = seatsService.updateSeat(id, request);
        return ResponseEntity.ok(seat);
    }
}
