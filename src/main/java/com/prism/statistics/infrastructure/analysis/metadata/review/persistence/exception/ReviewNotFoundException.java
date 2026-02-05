package com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException() {
        super("Review를 찾을 수 없습니다.");
    }
}
