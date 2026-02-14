package com.company.finflow.common.web;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Page;

@Value
@Builder
public class PaginatedResponse<T> {
    List<T> content;
    int pageNumber;
    int pageSize;
    int totalPages;
    long totalElements;

    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return PaginatedResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .build();
    }
}
