package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestStateHistory;
import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.vo.ReviewBody;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import com.prism.statistics.domain.statistics.repository.CollaborationStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.AuthorReviewWaitTimeDto;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.ReviewerResponseTimeDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.history.QPullRequestStateHistory.pullRequestStateHistory;
import static com.prism.statistics.domain.analysis.metadata.review.QRequestedReviewer.requestedReviewer;
import static com.prism.statistics.domain.analysis.metadata.review.QReview.review;
import static com.prism.statistics.domain.analysis.metadata.review.history.QRequestedReviewerHistory.requestedReviewerHistory;

@Repository
@RequiredArgsConstructor
public class CollaborationStatisticsRepositoryAdapter implements CollaborationStatisticsRepository {

    private static final String REVIEWER_KEY_SEPARATOR = "_";

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<CollaborationStatisticsDto> findCollaborationStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequest> pullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (pullRequests.isEmpty()) {
            return Optional.empty();
        }

        List<Long> pullRequestIds = pullRequests.stream()
                .map(pr -> pr.getId())
                .toList();

        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(review.pullRequestId.in(pullRequestIds))
                .fetch();

        List<PullRequestStateHistory> stateHistories = queryFactory
                .selectFrom(pullRequestStateHistory)
                .where(pullRequestStateHistory.pullRequestId.in(pullRequestIds))
                .fetch();

        List<RequestedReviewerHistory> reviewerHistories = queryFactory
                .selectFrom(requestedReviewerHistory)
                .where(requestedReviewerHistory.pullRequestId.in(pullRequestIds))
                .fetch();

        List<PullRequestBottleneck> bottlenecks = queryFactory
                .selectFrom(pullRequestBottleneck)
                .where(pullRequestBottleneck.pullRequestId.in(pullRequestIds))
                .fetch();

        List<RequestedReviewer> requestedReviewers = queryFactory
                .selectFrom(requestedReviewer)
                .where(requestedReviewer.pullRequestId.in(pullRequestIds))
                .fetch();

        return Optional.of(aggregateStatistics(
                pullRequests, reviews, stateHistories, reviewerHistories, bottlenecks, requestedReviewers
        ));
    }

    private CollaborationStatisticsDto aggregateStatistics(
            List<PullRequest> pullRequests,
            List<Review> reviews,
            List<PullRequestStateHistory> stateHistories,
            List<RequestedReviewerHistory> reviewerHistories,
            List<PullRequestBottleneck> bottlenecks,
            List<RequestedReviewer> requestedReviewers
    ) {
        long totalCount = pullRequests.size();

        Map<Long, PullRequest> prMap = pullRequests.stream()
                .collect(Collectors.toMap(pr -> pr.getId(), pr -> pr));

        Map<Long, PullRequestBottleneck> bottleneckMap = bottlenecks.stream()
                .collect(Collectors.toMap(bottleneck -> bottleneck.getPullRequestId(), b -> b));

        long reviewedCount = bottlenecks.stream()
                .filter(bottleneck -> bottleneck.hasReview())
                .count();

        Map<Long, Long> reviewerReviewCounts = calculateReviewerReviewCounts(reviews, requestedReviewers);

        long repeatedDraftPrCount = calculateRepeatedDraftPrCount(stateHistories);

        long reviewerAddedPrCount = calculateReviewerAddedPrCount(pullRequests, reviewerHistories);

        List<AuthorReviewWaitTimeDto> authorReviewWaitTimes = calculateAuthorReviewWaitTimes(
                pullRequests, bottleneckMap);

        List<ReviewerResponseTimeDto> reviewerResponseTimes = calculateReviewerResponseTimes(
                reviews, requestedReviewers);

        return new CollaborationStatisticsDto(
                totalCount,
                reviewedCount,
                reviewerReviewCounts,
                repeatedDraftPrCount,
                reviewerAddedPrCount,
                authorReviewWaitTimes,
                reviewerResponseTimes
        );
    }

    private Map<Long, Long> calculateReviewerReviewCounts(
            List<Review> reviews,
            List<RequestedReviewer> requestedReviewers
    ) {
        Map<Long, Long> counts = reviews.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getReviewer().getUserId(),
                        Collectors.counting()
                ));
        requestedReviewers.stream()
                .map(requested -> requested.getReviewer().getUserId())
                .forEach(reviewerId -> counts.putIfAbsent(reviewerId, 0L));
        return counts;
    }

    private long calculateRepeatedDraftPrCount(List<PullRequestStateHistory> stateHistories) {
        Map<Long, Long> draftOpenTransitionCounts = stateHistories.stream()
                .filter(history -> history.isDraftOpenTransition())
                .collect(Collectors.groupingBy(
                        history -> history.getPullRequestId(),
                        Collectors.counting()
                ));

        return draftOpenTransitionCounts.values().stream()
                .filter(count -> count >= 2)
                .count();
    }

    private long calculateReviewerAddedPrCount(
            List<PullRequest> pullRequests,
            List<RequestedReviewerHistory> reviewerHistories
    ) {
        Map<Long, LocalDateTime> prCreatedAtMap = pullRequests.stream()
                .collect(Collectors.toMap(
                        pr -> pr.getId(),
                        pr -> pr.getTiming().getGithubCreatedAt()
                ));

        return reviewerHistories.stream()
                .filter(h -> h.getAction() == ReviewerAction.REQUESTED)
                .filter(h -> {
                    LocalDateTime prCreatedAt = prCreatedAtMap.get(h.getPullRequestId());
                    return prCreatedAt != null && h.getGithubChangedAt().isAfter(prCreatedAt);
                })
                .map(history -> history.getPullRequestId())
                .distinct()
                .count();
    }

    private List<AuthorReviewWaitTimeDto> calculateAuthorReviewWaitTimes(
            List<PullRequest> pullRequests,
            Map<Long, PullRequestBottleneck> bottleneckMap
    ) {
        Map<Long, List<PullRequest>> prsByAuthor = pullRequests.stream()
                .collect(Collectors.groupingBy(pr -> pr.getAuthor().getUserId()));

        List<AuthorReviewWaitTimeDto> result = new ArrayList<>();

        for (Map.Entry<Long, List<PullRequest>> entry : prsByAuthor.entrySet()) {
            Long authorId = entry.getKey();
            List<PullRequest> authorPrs = entry.getValue();
            String authorName = authorPrs.get(0).getAuthor().getUserName();

            long totalReviewWaitMinutes = 0L;
            long prCount = 0L;

            for (PullRequest pr : authorPrs) {
                PullRequestBottleneck bottleneck = bottleneckMap.get(pr.getId());
                if (bottleneck != null && bottleneck.getReviewWait() != null) {
                    totalReviewWaitMinutes += bottleneck.getReviewWait().getMinutes();
                    prCount++;
                }
            }

            if (prCount > 0) {
                result.add(new AuthorReviewWaitTimeDto(authorId, authorName, totalReviewWaitMinutes, prCount));
            }
        }

        return result;
    }

    private List<ReviewerResponseTimeDto> calculateReviewerResponseTimes(
            List<Review> reviews,
            List<RequestedReviewer> requestedReviewers
    ) {
        Map<String, RequestedReviewer> requestedReviewerMap = requestedReviewers.stream()
                .collect(Collectors.toMap(
                        rr -> buildReviewerKey(rr.getPullRequestId(), rr.getReviewer().getUserId()),
                        rr -> rr,
                        (existing, replacement) -> existing
                ));

        Map<String, Review> firstReviewByPrAndReviewer = reviews.stream()
                .collect(Collectors.toMap(
                        r -> buildReviewerKey(r.getPullRequestId(), r.getReviewer().getUserId()),
                        r -> r,
                        (existing, replacement) ->
                                existing.getGithubSubmittedAt().isBefore(replacement.getGithubSubmittedAt())
                                        ? existing : replacement
                ));

        Map<Long, List<Review>> reviewsByReviewer = firstReviewByPrAndReviewer.values().stream()
                .collect(Collectors.groupingBy(r -> r.getReviewer().getUserId()));

        List<ReviewerResponseTimeDto> result = new ArrayList<>();

        for (Map.Entry<Long, List<Review>> entry : reviewsByReviewer.entrySet()) {
            Long reviewerId = entry.getKey();
            List<Review> reviewerReviews = entry.getValue();
            String reviewerName = reviewerReviews.get(0).getReviewer().getUserName();

            long totalResponseTimeMinutes = 0L;
            long reviewCount = 0L;

            for (Review rev : reviewerReviews) {
                if (isTrivialReview(rev)) {
                    continue;
                }
                String key = buildReviewerKey(rev.getPullRequestId(), reviewerId);
                RequestedReviewer requested = requestedReviewerMap.get(key);

                if (requested != null && requested.getGithubRequestedAt() != null) {
                    long responseMinutes = Duration.between(
                            requested.getGithubRequestedAt(),
                            rev.getGithubSubmittedAt()
                    ).toMinutes();

                    if (responseMinutes > 0) {
                        totalResponseTimeMinutes += responseMinutes;
                        reviewCount++;
                    }
                }
            }

            if (reviewCount > 0) {
                result.add(new ReviewerResponseTimeDto(reviewerId, reviewerName, totalResponseTimeMinutes, reviewCount));
            }
        }

        return result;
    }

    private boolean isTrivialReview(Review review) {
        if (review.getCommentCount() == 0) {
            return true;
        }

        ReviewBody body = review.getBody();
        if (body == null || body.isEmpty()) {
            return true;
        }

        String normalized = body.getValue().trim().toLowerCase();
        if (normalized.length() < 5) {
            return true;
        }

        return false;
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay())
                    .and(pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(1).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(1).atStartOfDay());
    }

    private String buildReviewerKey(Long pullRequestId, Long reviewerId) {
        return pullRequestId + REVIEWER_KEY_SEPARATOR + reviewerId;
    }
}
