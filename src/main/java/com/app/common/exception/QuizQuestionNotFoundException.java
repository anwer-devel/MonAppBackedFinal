package com.app.common.exception;

public class QuizQuestionNotFoundException extends RuntimeException {
    public QuizQuestionNotFoundException(String message) {
        super(message);
    }

    public QuizQuestionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

