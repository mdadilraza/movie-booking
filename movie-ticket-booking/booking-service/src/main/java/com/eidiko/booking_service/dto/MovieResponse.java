package com.eidiko.booking_service.dto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MovieResponse implements Serializable {
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
