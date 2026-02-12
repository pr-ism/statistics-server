package com.prism.statistics.application.analysis.metadata.review.event;

public record ReviewSavedEvent(Long githubReviewId, Long reviewId) {
}
