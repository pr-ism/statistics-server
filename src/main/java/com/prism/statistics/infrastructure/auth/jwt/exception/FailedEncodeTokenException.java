package com.prism.statistics.infrastructure.auth.jwt.exception;

public class FailedEncodeTokenException extends IllegalStateException {

    public FailedEncodeTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
