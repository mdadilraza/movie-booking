package com.eidiko.booking_service.service;

import com.eidiko.booking_service.dto.SeatRequest;
import com.eidiko.booking_service.entity.Seats;

public interface SeatService {
    Seats createSeat( SeatRequest request);

    Seats updateSeat(Long id, SeatRequest request);
}
