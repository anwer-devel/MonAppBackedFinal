package com.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    private final String code;
    private final String detail;

    public ConflictException(String message) {
        super(message);
        this.code = "CONFLICT";
        this.detail = message;
    }

    public ConflictException(String code, String detail) {
        super(detail);
        this.code = code;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }
}
