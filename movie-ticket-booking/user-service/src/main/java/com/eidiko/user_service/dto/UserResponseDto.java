package com.eidiko.user_service.dto;

public record UserResponseDto(long id,
                              String username,
                              String email
        , String fullName
        , String phoneNumber
        , String role) {
}
