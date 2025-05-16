package com.eidiko.booking_service.service;

import com.eidiko.booking_service.dto.SeatRequest;
import com.eidiko.booking_service.entity.SeatType;
import com.eidiko.booking_service.entity.Seats;
import com.eidiko.booking_service.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService{
    private final SeatRepository seatsRepository;

    @Transactional
    public Seats createSeat(SeatRequest request) {
        log.info("Creating seat type: {}, price: {}", request.getTypeName(), request.getBasePrice());

        // Validate SeatType enum
        try {
            SeatType.valueOf(request.getTypeName());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid seat type: " + request.getTypeName());
        }
        // Check for existing seat type
        if (seatsRepository.existsByTypeName(SeatType.valueOf(request.getTypeName()))) {
            throw new IllegalArgumentException("Seat type already exists: " + request.getTypeName());
        }
        Seats seat = new Seats();
        seat.setTypeName(SeatType.valueOf(request.getTypeName()));
        seat.setBasePrice(request.getBasePrice());
        Seats savedSeat = seatsRepository.save(seat);
        log.info("Seat type created with ID: {}", savedSeat.getId());
        return savedSeat;
    }

    @Transactional
    public Seats updateSeat(Long id, SeatRequest request) {
        log.info("Updating seat type with ID: {}, new price: {}", id, request.getBasePrice());

        Seats seat = seatsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Seat type with ID " + id + " not found"));

        // Validate SeatType enum
        try {
            SeatType.valueOf(request.getTypeName());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid seat type: " + request.getTypeName());
        }

        seat.setTypeName(SeatType.valueOf(request.getTypeName()));
        seat.setBasePrice(request.getBasePrice());
        Seats updatedSeat = seatsRepository.save(seat);
        log.info("Seat type updated with ID: {}", updatedSeat.getId());
        return updatedSeat;
    }
}
