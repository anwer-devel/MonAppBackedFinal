package com.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    private final String code;
    private final String detail;

    public ForbiddenException(String message) {
        super(message);
        this.code = "FORBIDDEN";
        this.detail = message;
    }

    public ForbiddenException(String code, String detail) {
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

