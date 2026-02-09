package com.prism.statistics.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RequestedReviewerErrorCode implements ErrorCode {

    REQUESTED_REVIEWER_NOT_FOUND("RR00", "요청된 리뷰어를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    RequestedReviewerErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
