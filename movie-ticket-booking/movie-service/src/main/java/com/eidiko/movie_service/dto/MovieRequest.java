package com.eidiko.movie_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MovieRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String genre;
    @NotBlank
    private Integer duration;
    @NotBlank
    private String releaseDate;
}
