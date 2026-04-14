package com.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    private final String code;
    private final String detail;

    public BadRequestException(String message) {
        super(message);
        this.code = "BAD_REQUEST";
        this.detail = message;
    }

    public BadRequestException(String code, String detail) {
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

