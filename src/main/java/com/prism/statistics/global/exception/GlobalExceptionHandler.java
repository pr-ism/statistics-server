package com.prism.statistics.global.exception;

import com.prism.statistics.global.exception.dto.response.AuthErrorCode;
import com.prism.statistics.global.exception.dto.response.ExceptionResponse;
import com.prism.statistics.presentation.auth.exception.RefreshTokenNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {
        log.error("Exception : ", ex);

        ExceptionResponse response = new ExceptionResponse("D00", "서버 에러");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(response);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleRefreshTokenNotFoundException() {
        ExceptionResponse response = ExceptionResponse.from(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(response);
    }
}
