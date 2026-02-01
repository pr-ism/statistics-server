package com.prism.statistics.application.webhook;

import com.prism.statistics.application.webhook.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.review.Review;
import com.prism.statistics.domain.review.enums.ReviewState;
import com.prism.statistics.domain.review.repository.ReviewRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewSubmittedService {

    private final Clock clock;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void submitReview(String apiKey, ReviewSubmittedRequest request) {
        validateApiKey(apiKey);

        Review review = createReview(request);
        reviewRepository.save(review);
    }

    private void validateApiKey(String apiKey) {
        projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());
    }

    private Review createReview(ReviewSubmittedRequest request) {
        ReviewState state = ReviewState.from(request.state());
        LocalDateTime submittedAt = toLocalDateTime(request.submittedAt());

        return Review.create(
                request.githubPullRequestId(),
                request.githubReviewId(),
                request.reviewer().login(),
                request.reviewer().id(),
                state,
                request.commitSha(),
                request.body(),
                request.commentCount(),
                submittedAt
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
