package com.eidiko.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @Email
    private String email;
    private String fullName;
    @NotNull
    @Size(max = 10)
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String phoneNumber;
    @NotNull
    private String role; // Optional, default to "USER" if not provided

}
