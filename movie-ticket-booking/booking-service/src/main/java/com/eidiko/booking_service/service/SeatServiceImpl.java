package com.eidiko.booking_service.service;

import com.eidiko.booking_service.dto.SeatRequest;
import com.eidiko.booking_service.entity.SeatType;
import com.eidiko.booking_service.entity.Seats;
import com.eidiko.booking_service.repository.SeatRepository;
import com.eidiko.booking_service.util.EnumValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatsRepository;

    @Override
    @Transactional
    public Seats createSeat(SeatRequest request) {
        if (request == null) throw new IllegalArgumentException("Seat request cannot be null");

        SeatType seatType = EnumValidator.validateEnum(SeatType.class, request.getTypeName(),
                "seat type");

        if (seatsRepository.existsByTypeName(seatType)) {
            throw new IllegalArgumentException("Seat type already exists: " + request.getTypeName());
        }

        Seats seat = new Seats();
        seat.setTypeName(seatType);
        seat.setBasePrice(request.getBasePrice());

        Seats savedSeat = seatsRepository.save(seat);
        log.info("Created seat type: {} with ID: {}", seatType, savedSeat.getId());
        return savedSeat;
    }


    @Transactional
    public Seats updateSeat(Long id, SeatRequest request) {
        if (request == null) throw new IllegalArgumentException("Seat Request Can not be null");
        log.info("Updating seat type with ID: {}, new price: {}", id, request.getBasePrice());

        Seats seat = seatsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Seat type with ID " + id + " not found"));
        //validate Seat type Enum
        SeatType seatType = EnumValidator.validateEnum(SeatType.class, request.getTypeName(),
                "seat type");
        seat.setTypeName(seatType);
        seat.setBasePrice(request.getBasePrice());
        Seats updatedSeat = seatsRepository.save(seat);
        log.info("Seat type updated with ID: {}", updatedSeat.getId());
        return updatedSeat;
    }
}
