package com.prism.statistics.global.exception.dto.response;

import lombok.Getter;

@Getter
public enum AuthErrorCode implements ErrorCode {

    REFRESH_TOKEN_NOT_FOUND("A00", "토큰 재발급 실패");

    private final String errorCode;
    private final String message;

    AuthErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
