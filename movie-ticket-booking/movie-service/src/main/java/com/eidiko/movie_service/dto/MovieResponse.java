package com.eidiko.movie_service.dto;
import lombok.Data;



@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer duration;
    private String releaseDate;
    private boolean isActive;
    private String posterUrl;
}
