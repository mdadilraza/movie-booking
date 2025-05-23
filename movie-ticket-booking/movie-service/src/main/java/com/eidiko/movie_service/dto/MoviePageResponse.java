package com.eidiko.movie_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoviePageResponse {
    private List<MovieResponse> movies;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;
    private String sortField;
    private String sortDirection;
}
