package com.eidiko.movie_service.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer duration;
    private String releaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}
