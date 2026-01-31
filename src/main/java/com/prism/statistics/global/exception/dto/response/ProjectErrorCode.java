package com.prism.statistics.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProjectErrorCode implements ErrorCode {

    PROJECT_NOT_FOUND("P00", "프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_API_KEY("P01", "유효하지 않은 API Key입니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    ProjectErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
