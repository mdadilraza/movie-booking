package com.eidiko.booking_service.dto;

import lombok.Data;

import java.util.Set;

@Data

public class CancelSeatRequest {
    private Set<String> seats;
}
