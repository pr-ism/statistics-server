package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
import com.prism.statistics.application.collect.inbox.aop.InboxEnqueue;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewCommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewCommentEditedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ReviewCommentRepository reviewCommentRepository;

    @InboxEnqueue(CollectInboxType.REVIEW_COMMENT_EDITED)
    public void editReviewComment(ReviewCommentEditedRequest request) {
        validateReviewCommentExists(request.githubCommentId());

        reviewCommentRepository.updateBodyIfLatest(
                request.githubCommentId(),
                request.body(),
                localDateTimeConverter.toLocalDateTime(request.updatedAt())
        );
    }

    private void validateReviewCommentExists(Long githubCommentId) {
        if (!reviewCommentRepository.existsByGithubCommentId(githubCommentId)) {
            throw new ReviewCommentNotFoundException();
        }
    }

}
