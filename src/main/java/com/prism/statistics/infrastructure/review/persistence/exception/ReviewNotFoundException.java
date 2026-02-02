package com.prism.statistics.infrastructure.review.persistence.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException() {
        super("Review를 찾을 수 없습니다.");
    }
}
