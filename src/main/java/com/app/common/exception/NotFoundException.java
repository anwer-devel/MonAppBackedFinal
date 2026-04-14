package com.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    private final String code;
    private final String detail;

    public NotFoundException(String message) {
        super(message);
        this.code = "NOT_FOUND";
        this.detail = message;
    }

    public NotFoundException(String code, String detail) {
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

