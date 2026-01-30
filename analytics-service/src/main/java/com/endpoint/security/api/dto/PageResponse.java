package com.endpoint.security.api.dto;

import java.util.List;

/**
 * Industry-standard paginated response: total count, total pages, current page (1-based), size, and content.
 */
public record PageResponse<T>(
    long totalResultCount,
    int totalPages,
    int currentPage,
    int countPerPage,
    List<T> content
) {
    public static <T> PageResponse<T> of(long totalResultCount, int totalPages, int currentPage, int countPerPage, List<T> content) {
        return new PageResponse<>(totalResultCount, totalPages, currentPage, countPerPage, content);
    }
}
