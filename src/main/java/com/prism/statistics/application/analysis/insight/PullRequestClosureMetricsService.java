package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.activity.repository.ReviewActivityRepository;
import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.analysis.insight.lifecycle.repository.PullRequestLifecycleRepository;
import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PullRequestClosureMetricsService {

    private final PullRequestRepository pullRequestRepository;
    private final ReviewRepository reviewRepository;
    private final PullRequestLifecycleRepository lifecycleRepository;
    private final ReviewActivityRepository reviewActivityRepository;

    @Transactional
    public void deriveClosureMetrics(Long pullRequestId, PullRequestState newState, LocalDateTime closedAt) {
        if (!isClosureState(newState)) {
            return;
        }

        PullRequest pullRequest = pullRequestRepository.findById(pullRequestId)
                .orElseThrow(() -> new IllegalArgumentException("PullRequest not found: " + pullRequestId));

        List<Review> reviews = reviewRepository.findAllByPullRequestId(pullRequestId);

        saveOrUpdateLifecycle(pullRequest, newState, closedAt, reviews.isEmpty());
        saveReviewActivity(pullRequest, reviews);
    }

    private boolean isClosureState(PullRequestState state) {
        return state == PullRequestState.MERGED || state == PullRequestState.CLOSED;
    }

    private void saveOrUpdateLifecycle(
            PullRequest pullRequest,
            PullRequestState newState,
            LocalDateTime closedAt,
            boolean closedWithoutReview
    ) {
        PullRequestTiming timing = pullRequest.getTiming();
        LocalDateTime createdAt = timing.getGithubCreatedAt();

        DurationMinutes timeToMerge = (newState == PullRequestState.MERGED)
                ? DurationMinutes.between(createdAt, closedAt)
                : null;

        DurationMinutes totalLifespan = DurationMinutes.between(createdAt, closedAt);
        DurationMinutes activeWork = totalLifespan;

        DurationMinutes finalTimeToMerge = timeToMerge;
        lifecycleRepository.findByPullRequestId(pullRequest.getId())
                .ifPresentOrElse(
                        lifecycle -> lifecycle.updateOnClose(
                                finalTimeToMerge,
                                totalLifespan,
                                activeWork,
                                closedWithoutReview
                        ),
                        () -> lifecycleRepository.save(createNewLifecycle(
                                pullRequest.getId(),
                                createdAt,
                                finalTimeToMerge,
                                totalLifespan,
                                activeWork,
                                closedWithoutReview
                        ))
                );
    }

    private PullRequestLifecycle createNewLifecycle(
            Long pullRequestId,
            LocalDateTime reviewReadyAt,
            DurationMinutes timeToMerge,
            DurationMinutes totalLifespan,
            DurationMinutes activeWork,
            boolean closedWithoutReview
    ) {
        return PullRequestLifecycle.builder()
                .pullRequestId(pullRequestId)
                .reviewReadyAt(reviewReadyAt)
                .timeToMerge(timeToMerge)
                .totalLifespan(totalLifespan)
                .activeWork(activeWork)
                .stateChangeCount(1)
                .reopened(false)
                .closedWithoutReview(closedWithoutReview)
                .build();
    }

    private void saveReviewActivity(PullRequest pullRequest, List<Review> reviews) {
        if (reviewActivityRepository.existsByPullRequestId(pullRequest.getId())) {
            return;
        }

        PullRequestChangeStats changeStats = pullRequest.getChangeStats();
        int totalAdditions = changeStats.getAdditionCount();
        int totalDeletions = changeStats.getDeletionCount();

        if (reviews.isEmpty()) {
            ReviewActivity activity = ReviewActivity.createWithoutReview(
                    pullRequest.getId(),
                    totalAdditions,
                    totalDeletions
            );
            reviewActivityRepository.save(activity);
            return;
        }

        int reviewRoundTrips = reviews.size();
        int totalCommentCount = reviews.stream()
                .mapToInt(Review::getCommentCount)
                .sum();

        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequest.getId())
                .reviewRoundTrips(reviewRoundTrips)
                .totalCommentCount(totalCommentCount)
                .codeAdditionsAfterReview(0)
                .codeDeletionsAfterReview(0)
                .additionalReviewerCount(countUniqueReviewers(reviews) - 1)
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .build();

        reviewActivityRepository.save(activity);
    }

    private int countUniqueReviewers(List<Review> reviews) {
        return (int) reviews.stream()
                .map(review -> review.getReviewer().getUserId())
                .distinct()
                .count();
    }
}
