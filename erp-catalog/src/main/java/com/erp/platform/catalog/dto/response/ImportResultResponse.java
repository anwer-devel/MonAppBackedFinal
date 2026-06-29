package com.erp.platform.catalog.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ImportResultResponse {
    private int imported;
    private int skipped;
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ImportError {
        private int line;
        private String ref;
        private String reason;
    }
}
