package com.erp.platform.core.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageResponse<T> of(List<T> content, long totalElements,
                                          int totalPages, int page, int size) {
        return PageResponse.<T>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();
    }

    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> springPage) {
        return PageResponse.<T>builder()
                .content(springPage.getContent())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .page(springPage.getNumber())
                .size(springPage.getSize())
                .build();
    }
}
