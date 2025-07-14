package com.eidiko.booking_service.util;

import com.eidiko.booking_service.entity.SeatType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumValidatorTest {

    @Test
    void shouldReturnValidEnumConstant() {
        SeatType result = EnumValidator.validateEnum(SeatType.class, "STANDARD", "seat type");
        assertEquals(SeatType.STANDARD, result);
    }

    @Test
    void shouldThrowForInvalidEnum() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                EnumValidator.validateEnum(SeatType.class, "INVALID", "seat type"));

        assertTrue(exception.getMessage().contains("Invalid seat type"));
    }

    @Test
    void shouldThrowForNullInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                EnumValidator.validateEnum(SeatType.class, null, "seat type"));

        assertTrue(exception.getMessage().contains("cannot be null"));
    }
}
