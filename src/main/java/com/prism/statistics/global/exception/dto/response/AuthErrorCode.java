package com.prism.statistics.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    REFRESH_TOKEN_NOT_FOUND("A00", "토큰 재발급 실패", HttpStatus.UNAUTHORIZED),
    WITHDRAWN_USER("A01", "인증 실패", HttpStatus.UNAUTHORIZED),
    ORPHAN_USER_IDENTITY("A02", "로그인 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_MISSING("A03", "로그인 실패", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
