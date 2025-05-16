package com.eidiko.movie_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class MovieRequest {
    @NotBlank(message = "title should not be blank or empty")
    private String title;

    @NotBlank(message = "description should not be blank or empty")
    private String description;

    @NotBlank(message = "genre should not be blank or empty")
    private String genre;

    @NotBlank(message = "duration should not be blank or empty")
    private Integer duration;

    @NotBlank(message = "releaseDate should not be blank or empty")
    private String releaseDate;

    @NotBlank(message = "moviePoster should not be blank or empty")
    private MultipartFile moviePoster;
}
