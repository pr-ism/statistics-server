package com.prism.statistics.domain.analysis.metadata.review.exception;

public class ReviewCommentNotFoundException extends RuntimeException {

    public ReviewCommentNotFoundException() {
        super("리뷰 댓글을 찾을 수 없습니다.");
    }
}
