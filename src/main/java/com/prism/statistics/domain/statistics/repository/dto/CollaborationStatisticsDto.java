package com.prism.statistics.domain.statistics.repository.dto;

import java.util.List;
import java.util.Map;

public record CollaborationStatisticsDto(
        long totalCount,
        long reviewedCount,
        Map<Long, Long> reviewerReviewCounts,
        long repeatedDraftPrCount,
        long reviewerAddedPrCount,
        List<AuthorReviewWaitTimeDto> authorReviewWaitTimes,
        List<ReviewerResponseTimeDto> reviewerResponseTimes
) {

    public record AuthorReviewWaitTimeDto(
            Long authorId,
            String authorName,
            long totalReviewWaitMinutes,
            long prCount
    ) {
    }

    public record ReviewerResponseTimeDto(
            Long reviewerId,
            String reviewerName,
            long totalResponseTimeMinutes,
            long reviewCount
    ) {
    }
}
