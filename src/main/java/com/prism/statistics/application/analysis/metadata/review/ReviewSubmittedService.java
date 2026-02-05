package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewSubmittedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;

    public void submitReview(String apiKey, ReviewSubmittedRequest request) {
        validateApiKey(apiKey);

        Review review = createReview(request);
        reviewRepository.saveOrFind(review);
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }

    private Review createReview(ReviewSubmittedRequest request) {
        ReviewState state = ReviewState.from(request.state());
        LocalDateTime submittedAt = localDateTimeConverter.toLocalDateTime(request.submittedAt());

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
}
