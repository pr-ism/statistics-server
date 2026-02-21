package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.insight.review.repository.ReviewResponseTimeRepository;
import com.prism.statistics.domain.analysis.insight.review.repository.ReviewSessionRepository;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewActivityMetricsService {

    private final ReviewRepository reviewRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewResponseTimeRepository reviewResponseTimeRepository;

    @Transactional
    public void deriveMetrics(Long reviewId) {
        Review review = reviewRepository.findByGithubReviewId(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        if (!review.hasAssignedPullRequest()) {
            return;
        }

        createOrUpdateReviewSession(review);
        handleReviewResponseTime(review);
    }

    @Transactional
    public void deriveMetricsByGithubReviewId(Long githubReviewId) {
        reviewRepository.findByGithubReviewId(githubReviewId)
                .ifPresent(review -> {
                    if (review.hasAssignedPullRequest()) {
                        createOrUpdateReviewSession(review);
                        handleReviewResponseTime(review);
                    }
                });
    }

    private void createOrUpdateReviewSession(Review review) {
        Long pullRequestId = review.getPullRequestId();
        GithubUser reviewer = review.getReviewer();
        LocalDateTime reviewedAt = review.getGithubSubmittedAt();
        int commentCount = review.getCommentCount();

        reviewSessionRepository.findByReviewer(pullRequestId, reviewer.getUserId())
                .ifPresentOrElse(
                        session -> session.updateOnReview(reviewedAt, commentCount),
                        () -> reviewSessionRepository.save(
                                createNewReviewSession(pullRequestId, reviewer, reviewedAt)
                        )
                );
    }

    private ReviewSession createNewReviewSession(
            Long pullRequestId,
            GithubUser reviewer,
            LocalDateTime reviewedAt
    ) {
        return ReviewSession.create(pullRequestId, reviewer, reviewedAt);
    }

    private void handleReviewResponseTime(Review review) {
        ReviewState state = review.getReviewState();
        Long pullRequestId = review.getPullRequestId();
        LocalDateTime reviewedAt = review.getGithubSubmittedAt();

        if (state.isChangesRequested()) {
            handleChangesRequested(pullRequestId, reviewedAt);
            return;
        }

        if (state.isApproved()) {
            handleApproval(pullRequestId, reviewedAt);
        }
    }

    private void handleChangesRequested(Long pullRequestId, LocalDateTime changesRequestedAt) {
        reviewResponseTimeRepository.findByPullRequestId(pullRequestId)
                .ifPresentOrElse(
                        responseTime -> responseTime.updateOnChangesRequested(changesRequestedAt),
                        () -> reviewResponseTimeRepository.save(
                                ReviewResponseTime.createOnChangesRequested(pullRequestId, changesRequestedAt)
                        )
                );
    }

    private void handleApproval(Long pullRequestId, LocalDateTime approvedAt) {
        reviewResponseTimeRepository.findByPullRequestId(pullRequestId)
                .ifPresent(responseTime -> {
                    if (responseTime.hasChangesRequested() && !responseTime.isResolved()) {
                        responseTime.updateOnApproveAfterChanges(approvedAt);
                    }
                });
    }
}
