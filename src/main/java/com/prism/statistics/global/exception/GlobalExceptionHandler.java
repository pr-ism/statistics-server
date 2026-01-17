package com.prism.statistics.global.exception;

import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.domain.user.exception.AlreadyWithdrawnUserException;
import com.prism.statistics.global.exception.dto.response.AuthErrorCode;
import com.prism.statistics.global.exception.dto.response.DefaultErrorCode;
import com.prism.statistics.global.exception.dto.response.ErrorCode;
import com.prism.statistics.global.exception.dto.response.ExceptionResponse;
import com.prism.statistics.global.exception.dto.response.UserErrorCode;
import com.prism.statistics.presentation.auth.exception.RefreshTokenNotFoundException;
import lombok.extern.slf4j.Slf4j;
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

        return createResponseEntity(DefaultErrorCode.UNKNOWN_SERVER_EXCEPTION);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.info("IllegalArgumentException : {}", ex.getMessage());

        return createResponseEntity(DefaultErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalStateException() {
        return createResponseEntity(DefaultErrorCode.INVALID_INPUT_STATE);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleRefreshTokenNotFoundException() {
        return createResponseEntity(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @ExceptionHandler(WithdrawnUserLoginException.class)
    public ResponseEntity<ExceptionResponse> handleWithdrawnUserLoginException() {
        return createResponseEntity(AuthErrorCode.WITHDRAWN_USER);
    }

    @ExceptionHandler(AlreadyWithdrawnUserException.class)
    public ResponseEntity<ExceptionResponse> handleAlreadyWithdrawnUserException() {
        return createResponseEntity(UserErrorCode.ALREADY_WITHDRAWN);
    }

    private ResponseEntity<ExceptionResponse> createResponseEntity(ErrorCode errorCode) {
        ExceptionResponse response = ExceptionResponse.from(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus())
                             .body(response);
    }
}
