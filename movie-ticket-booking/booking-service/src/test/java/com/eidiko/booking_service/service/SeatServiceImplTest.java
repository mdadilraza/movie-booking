package com.eidiko.booking_service.service;
import com.eidiko.booking_service.dto.SeatRequest;
import com.eidiko.booking_service.entity.SeatType;
import com.eidiko.booking_service.entity.Seats;
import com.eidiko.booking_service.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    @Test
    void testCreateSeat_success() {
        SeatRequest request = new SeatRequest("STANDARD", 250.0);
        when(seatRepository.existsByTypeName(SeatType.STANDARD)).thenReturn(false);

        Seats savedSeat = new Seats();
        savedSeat.setId(1L);
        savedSeat.setTypeName(SeatType.STANDARD);
        savedSeat.setBasePrice(250.0);

        when(seatRepository.save(any())).thenReturn(savedSeat);

        Seats result = seatService.createSeat(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(SeatType.STANDARD, result.getTypeName());
        assertEquals(250.0, result.getBasePrice());
    }

    @Test
    void testCreateSeat_invalidEnum_shouldThrowException() {
        SeatRequest request = new SeatRequest("INVALID", 400.0);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seatService.createSeat(request);
        });

        assertTrue(ex.getMessage().contains("Invalid seat type"));
    }
    @Test
    void testCreateSeat_nullRequest_shouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seatService.createSeat(null);
        });

        assertTrue(ex.getMessage().contains("cannot be null"));
    }


    @Test
    void testUpdateSeat_success() {
        SeatRequest request = new SeatRequest("VIP", 999.0);
        Long seatId = 1L;

        Seats existingSeat = new Seats();
        existingSeat.setId(seatId);
        existingSeat.setTypeName(SeatType.STANDARD);
        existingSeat.setBasePrice(500.0);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Seats result = seatService.updateSeat(seatId, request);

        assertNotNull(result);
        assertEquals(SeatType.VIP, result.getTypeName());
        assertEquals(999.0, result.getBasePrice());
    }

    @Test
    void testUpdateSeat_notFound_shouldThrowException() {
        Long seatId = 99L;
        SeatRequest request = new SeatRequest("STANDARD", 500.0);

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class, () -> {
            seatService.updateSeat(seatId, request);
        });

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testCreateSeat_duplicateSeat_shouldThrowException() {
        SeatRequest request = new SeatRequest("VIP", 800.0);

        when(seatRepository.existsByTypeName(SeatType.VIP)).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seatService.createSeat(request);
        });

        assertTrue(ex.getMessage().contains("already exists"));
    }
    @Test
    void testUpdateSeat_nullRequest_shouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seatService.updateSeat(1L, null);
        });

        assertTrue(ex.getMessage().contains("Seat Request Can not be null"));
    }

}
