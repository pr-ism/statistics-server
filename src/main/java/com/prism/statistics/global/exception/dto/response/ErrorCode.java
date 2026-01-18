package com.prism.statistics.global.exception.dto.response;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getErrorCode();

    String getMessage();

    HttpStatus getHttpStatus();
}
