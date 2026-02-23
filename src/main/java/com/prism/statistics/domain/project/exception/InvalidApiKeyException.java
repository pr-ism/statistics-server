package com.prism.statistics.domain.project.exception;

public class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException() {
        super("유효하지 않은 API Key입니다.");
    }
}
