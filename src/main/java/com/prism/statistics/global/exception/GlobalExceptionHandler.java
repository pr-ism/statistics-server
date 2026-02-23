package com.prism.statistics.global.exception;

import com.prism.statistics.application.auth.exception.UserMissingException;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.application.user.exception.UserNotFoundException;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.exception.ProjectSettingNotFoundException;
import com.prism.statistics.domain.user.exception.AlreadyWithdrawnUserException;
import com.prism.statistics.global.exception.dto.response.AuthErrorCode;
import com.prism.statistics.global.exception.dto.response.CommitErrorCode;
import com.prism.statistics.global.exception.dto.response.DefaultErrorCode;
import com.prism.statistics.global.exception.dto.response.ErrorCode;
import com.prism.statistics.global.exception.dto.response.ExceptionResponse;
import com.prism.statistics.global.exception.dto.response.ProjectErrorCode;
import com.prism.statistics.global.exception.dto.response.PullRequestErrorCode;
import com.prism.statistics.global.exception.dto.response.PullRequestLabelErrorCode;
import com.prism.statistics.global.exception.dto.response.RequestedReviewerErrorCode;
import com.prism.statistics.global.exception.dto.response.ReviewCommentErrorCode;
import com.prism.statistics.global.exception.dto.response.ReviewErrorCode;
import com.prism.statistics.global.exception.dto.response.UserErrorCode;
import com.prism.statistics.domain.analysis.metadata.pullrequest.exception.HeadCommitNotFoundException;
import com.prism.statistics.domain.analysis.metadata.pullrequest.exception.PullRequestNotFoundException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.RequestedReviewerNotFoundException;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewCommentNotFoundException;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewNotFoundException;
import com.prism.statistics.infrastructure.auth.persistence.exception.OrphanedUserIdentityException;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestLabelNotFoundException;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.domain.project.exception.ProjectNotFoundException;
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

    @ExceptionHandler(ProjectSettingNotFoundException.class)
    public ResponseEntity<Object> handleProjectSettingNotFoundException(ProjectSettingNotFoundException ex) {
        log.info("ProjectSettingNotFoundException : {}", ex.getMessage());
        return createResponseEntity(ProjectErrorCode.PROJECT_SETTINGS_NOT_FOUND);
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        log.info("UserNotFoundException : {}", ex.getMessage());

        return createResponseEntity(UserErrorCode.USER_NOT_FOUND);
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

    @ExceptionHandler(com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException.class)
    public ResponseEntity<Object> handleInfraProjectNotFoundException(
            com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException ex
    ) {
        log.info("InfraProjectNotFoundException : {}", ex.getMessage());

        return createResponseEntity(ProjectErrorCode.PROJECT_NOT_FOUND);
    }

    @ExceptionHandler(ProjectOwnershipException.class)
    public ResponseEntity<Object> handleProjectOwnershipException(ProjectOwnershipException ex) {
        log.info("ProjectOwnershipException : {}", ex.getMessage());

        return createResponseEntity(ProjectErrorCode.PROJECT_NOT_FOUND);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<Object> handleInvalidApiKeyException(InvalidApiKeyException ex) {
        log.info("InvalidApiKeyException : {}", ex.getMessage());

        return createResponseEntity(ProjectErrorCode.INVALID_API_KEY);
    }

    @ExceptionHandler(HeadCommitNotFoundException.class)
    public ResponseEntity<Object> handleHeadCommitNotFoundException(HeadCommitNotFoundException ex) {
        log.info("HeadCommitNotFoundException : {}", ex.getMessage());

        return createResponseEntity(CommitErrorCode.HEAD_COMMIT_NOT_FOUND);
    }

    @ExceptionHandler(PullRequestNotFoundException.class)
    public ResponseEntity<Object> handlePullRequestNotFoundException(PullRequestNotFoundException ex) {
        log.info("PullRequestNotFoundException : {}", ex.getMessage());

        return createResponseEntity(PullRequestErrorCode.PULL_REQUEST_NOT_FOUND);
    }

    @ExceptionHandler(com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestNotFoundException.class)
    public ResponseEntity<Object> handleInfraPullRequestNotFoundException(
            com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestNotFoundException ex
    ) {
        log.info("InfraPullRequestNotFoundException : {}", ex.getMessage());

        return createResponseEntity(PullRequestErrorCode.PULL_REQUEST_NOT_FOUND);
    }

    @ExceptionHandler(PullRequestLabelNotFoundException.class)
    public ResponseEntity<Object> handlePullRequestLabelNotFoundException(PullRequestLabelNotFoundException ex) {
        log.info("PullRequestLabelNotFoundException : {}", ex.getMessage());

        return createResponseEntity(PullRequestLabelErrorCode.PULL_REQUEST_LABEL_NOT_FOUND);
    }

    @ExceptionHandler(ReviewCommentNotFoundException.class)
    public ResponseEntity<Object> handleReviewCommentNotFoundException(ReviewCommentNotFoundException ex) {
        log.info("ReviewCommentNotFoundException : {}", ex.getMessage());

        return createResponseEntity(ReviewCommentErrorCode.REVIEW_COMMENT_NOT_FOUND);
    }

    @ExceptionHandler(RequestedReviewerNotFoundException.class)
    public ResponseEntity<Object> handleRequestedReviewerNotFoundException(RequestedReviewerNotFoundException ex) {
        log.info("RequestedReviewerNotFoundException : {}", ex.getMessage());

        return createResponseEntity(RequestedReviewerErrorCode.REQUESTED_REVIEWER_NOT_FOUND);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<Object> handleReviewNotFoundException(ReviewNotFoundException ex) {
        log.info("ReviewNotFoundException : {}", ex.getMessage());

        return createResponseEntity(ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @ExceptionHandler(com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.ReviewNotFoundException.class)
    public ResponseEntity<Object> handleInfraReviewNotFoundException(
            com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.ReviewNotFoundException ex
    ) {
        log.info("InfraReviewNotFoundException : {}", ex.getMessage());

        return createResponseEntity(ReviewErrorCode.REVIEW_NOT_FOUND);
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
