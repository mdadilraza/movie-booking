package com.eidiko.booking_service.controller;

import com.eidiko.booking_service.dto.*;
import com.eidiko.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping(value = "/showtimes" )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowtimeResponse> createShowtime(@Valid @RequestBody ShowtimeRequest showtimeRequest) {
        ShowtimeResponse response = bookingService.createShowtime(showtimeRequest);
        log.info("Response from createShowtime: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.createBooking(bookingRequest));
    }

    @PostMapping("/cancelSeats/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<BookingResponse>> cancelSeats(@PathVariable long bookingId, @RequestBody CancelSeatRequest cancelSeatRequest) {
        return ResponseEntity.ok(bookingService.cancelSeats(bookingId, cancelSeatRequest));
    }

    @GetMapping("/showtime/movie/{movieId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<ShowtimeResponse>> showtimeByMovie(@PathVariable long movieId) {
        return ResponseEntity.ok(bookingService.getShowtimeByMovie(movieId));
    }

}
