package com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception;

public class ReviewCommentNotFoundException extends RuntimeException {

    public ReviewCommentNotFoundException() {
        super("리뷰 댓글을 찾을 수 없습니다.");
    }
}
