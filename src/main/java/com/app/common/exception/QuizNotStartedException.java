package com.app.common.exception;

public class QuizNotStartedException extends RuntimeException {
    public QuizNotStartedException(String message) {
        super(message);
    }

    public QuizNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}

