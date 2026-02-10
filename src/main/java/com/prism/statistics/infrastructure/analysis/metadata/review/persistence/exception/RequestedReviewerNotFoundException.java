package com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception;

public class RequestedReviewerNotFoundException extends RuntimeException {

    public RequestedReviewerNotFoundException() {
        super("RequestedReviewer를 찾을 수 없습니다.");
    }
}
