package com.prism.statistics.global.exception;

import com.prism.statistics.application.auth.exception.UserMissingException;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.domain.user.exception.AlreadyWithdrawnUserException;
import com.prism.statistics.global.exception.dto.response.AuthErrorCode;
import com.prism.statistics.global.exception.dto.response.DefaultErrorCode;
import com.prism.statistics.global.exception.dto.response.ErrorCode;
import com.prism.statistics.global.exception.dto.response.ExceptionResponse;
import com.prism.statistics.global.exception.dto.response.ProjectErrorCode;
import com.prism.statistics.global.exception.dto.response.UserErrorCode;
import com.prism.statistics.infrastructure.auth.persistence.exception.OrphanedUserIdentityException;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.presentation.auth.exception.RefreshTokenNotFoundException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("Exception : ", ex);

        return createResponseEntity(DefaultErrorCode.UNKNOWN_SERVER_EXCEPTION);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.info("IllegalArgumentException : {}", ex.getMessage());

        return createResponseEntity(DefaultErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        log.info("IllegalStateException : {}", ex.getMessage());

        return createResponseEntity(DefaultErrorCode.INVALID_INPUT_STATE);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<Object> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex) {
        log.info("RefreshTokenNotFoundException : {}", ex.getMessage());

        return createResponseEntity(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @ExceptionHandler(WithdrawnUserLoginException.class)
    public ResponseEntity<Object> handleWithdrawnUserLoginException(WithdrawnUserLoginException ex) {
        log.info("WithdrawnUserLoginException : {}", ex.getMessage());

        return createResponseEntity(AuthErrorCode.WITHDRAWN_USER);
    }

    @ExceptionHandler(AlreadyWithdrawnUserException.class)
    public ResponseEntity<Object> handleAlreadyWithdrawnUserException(AlreadyWithdrawnUserException ex) {
        log.info("AlreadyWithdrawnUserException : {}", ex.getMessage());

        return createResponseEntity(UserErrorCode.ALREADY_WITHDRAWN);
    }

    @ExceptionHandler(OrphanedUserIdentityException.class)
    public ResponseEntity<Object> handleOrphanedUserIdentityException(OrphanedUserIdentityException ex) {
        log.info("OrphanedUserIdentityException : {}", ex.getMessage());

        return createResponseEntity(AuthErrorCode.ORPHAN_USER_IDENTITY);
    }

    @ExceptionHandler(UserMissingException.class)
    public ResponseEntity<Object> handleUserMissingException(UserMissingException ex) {
        log.info("UserMissingException : {}", ex.getMessage());

        return createResponseEntity(AuthErrorCode.USER_MISSING);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Object> handleProjectNotFoundException(ProjectNotFoundException ex) {
        log.info("ProjectNotFoundException : {}", ex.getMessage());

        return createResponseEntity(ProjectErrorCode.PROJECT_NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Object> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException ex
    ) {
        log.info("AuthenticationCredentialsNotFoundException : {}", ex.getMessage());

        return createResponseEntity(AuthErrorCode.FORBIDDEN_USER);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String errorMessage = ex.getBindingResult()
                                .getAllErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.joining(","));
        DefaultErrorCode errorCode = DefaultErrorCode.API_ARGUMENTS_NOT_VALID;
        ExceptionResponse response = new ExceptionResponse(
                errorCode.getErrorCode(),
                errorMessage
        );

        return handleExceptionInternal(
                ex,
                response,
                headers,
                errorCode.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> createResponseEntity(ErrorCode errorCode) {
        ExceptionResponse response = ExceptionResponse.from(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus())
                             .body(response);
    }
}
