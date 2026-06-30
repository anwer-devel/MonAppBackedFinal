package com.erp.platform.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    private final String field;

    public ConflictException(String message) {
        super(message);
        this.field = null;
    }

    public ConflictException(String message, String field) {
        super(message);
        this.field = field;
    }

    public ConflictException(String code, String field, String message) {
        super(message);
        this.field = field;
    }
}
