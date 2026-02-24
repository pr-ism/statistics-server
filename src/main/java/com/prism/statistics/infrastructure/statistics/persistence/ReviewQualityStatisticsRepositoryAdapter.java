package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.review.QReviewResponseTime.reviewResponseTime;
import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;
import static com.prism.statistics.domain.analysis.metadata.review.QReview.review;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.statistics.repository.ReviewQualityStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewActivityStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSessionStatisticsDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewQualityStatisticsRepositoryAdapter implements ReviewQualityStatisticsRepository {

    private static final long ZERO_COUNT = 0L;
    private static final long DATE_RANGE_INCLUSIVE_DAYS = 1L;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewActivityStatisticsDto> findReviewActivityStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ReviewActivity> activities = queryFactory
                .selectFrom(reviewActivity)
                .join(pullRequest).on(pullRequest.id.eq(reviewActivity.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        activityDateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (activities.isEmpty()) {
            return Optional.empty();
        }

        List<Long> pullRequestIds = activities.stream()
                .map(activity -> activity.getPullRequestId())
                .toList();

        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(
                        review.pullRequestId.in(pullRequestIds),
                        reviewDateRangeCondition(startDate, endDate)
                )
                .fetch();

        List<ReviewResponseTime> responseTimes = queryFactory
                .selectFrom(reviewResponseTime)
                .where(
                        reviewResponseTime.pullRequestId.in(pullRequestIds),
                        responseTimeDateRangeCondition(startDate, endDate)
                )
                .fetch();

        return Optional.of(aggregateActivityStatistics(activities, reviews, responseTimes));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewSessionStatisticsDto> findReviewSessionStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ReviewSession> sessions = queryFactory
                .selectFrom(reviewSession)
                .join(pullRequest).on(pullRequest.id.eq(reviewSession.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        sessionDateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (sessions.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(aggregateSessionStatistics(sessions));
    }

    private ReviewActivityStatisticsDto aggregateActivityStatistics(
            List<ReviewActivity> activities,
            List<Review> reviews,
            List<ReviewResponseTime> responseTimes
    ) {
        long totalCount = activities.size();

        long reviewedCount = activities.stream()
                .filter(activity -> activity.hasReviewActivity())
                .count();

        long totalReviewRoundTrips = activities.stream()
                .mapToLong(activity -> activity.getReviewRoundTrips())
                .sum();

        long totalCommentCount = activities.stream()
                .mapToLong(activity -> activity.getTotalCommentCount())
                .sum();

        BigDecimal totalCommentDensity = activities.stream()
                .map(activity -> activity.getCommentDensity())
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right));

        long withAdditionalReviewersCount = activities.stream()
                .filter(activity -> activity.isHasAdditionalReviewers())
                .count();

        long withChangesAfterReviewCount = activities.stream()
                .filter(activity -> activity.hasSignificantChangesAfterReview())
                .count();

        long firstReviewApproveCount = calculateFirstReviewApproveCount(reviews);
        long changesRequestedCount = calculateChangesRequestedCount(reviews);
        long totalChangesResolutionMinutes = calculateTotalChangesResolutionMinutes(responseTimes);
        long changesResolvedCount = calculateChangesResolvedCount(responseTimes);
        long highIntensityPrCount = calculateHighIntensityPrCount(activities);

        return new ReviewActivityStatisticsDto(
                totalCount,
                reviewedCount,
                totalReviewRoundTrips,
                totalCommentCount,
                totalCommentDensity,
                withAdditionalReviewersCount,
                withChangesAfterReviewCount,
                firstReviewApproveCount,
                changesRequestedCount,
                totalChangesResolutionMinutes,
                changesResolvedCount,
                highIntensityPrCount
        );
    }

    private long calculateFirstReviewApproveCount(List<Review> reviews) {
        Map<Long, Review> firstReviewByPr = reviews.stream()
                .collect(Collectors.toMap(
                        review -> review.getPullRequestId(),
                        r -> r,
                        (existing, replacement) -> selectEarlierReview(existing, replacement)
                ));

        return firstReviewByPr.values().stream()
                .filter(r -> r.getReviewState() == ReviewState.APPROVED)
                .count();
    }

    private long calculateChangesRequestedCount(List<Review> reviews) {
        return reviews.stream()
                .filter(r -> r.getReviewState() == ReviewState.CHANGES_REQUESTED)
                .map(review -> review.getPullRequestId())
                .distinct()
                .count();
    }

    private long calculateTotalChangesResolutionMinutes(List<ReviewResponseTime> responseTimes) {
        return responseTimes.stream()
                .filter(responseTime -> isResolved(responseTime))
                .mapToLong(rt -> rt.getChangesResolution().getMinutes())
                .sum();
    }

    private long calculateChangesResolvedCount(List<ReviewResponseTime> responseTimes) {
        return responseTimes.stream()
                .filter(responseTime -> isResolved(responseTime))
                .count();
    }

    private ReviewSessionStatisticsDto aggregateSessionStatistics(List<ReviewSession> sessions) {
        long totalSessionCount = sessions.size();

        long uniqueReviewerCount = sessions.stream()
                .map(s -> s.getReviewer().getUserId())
                .distinct()
                .count();

        long uniquePullRequestCount = sessions.stream()
                .map(session -> session.getPullRequestId())
                .distinct()
                .count();

        long totalSessionDurationMinutes = sessions.stream()
                .filter(s -> s.getSessionDuration() != null)
                .mapToLong(s -> s.getSessionDuration().getMinutes())
                .sum();

        long totalReviewCount = sessions.stream()
                .mapToLong(session -> session.getReviewCount())
                .sum();

        return new ReviewSessionStatisticsDto(
                totalSessionCount,
                uniqueReviewerCount,
                uniquePullRequestCount,
                totalSessionDurationMinutes,
                totalReviewCount
        );
    }

    private long calculateHighIntensityPrCount(List<ReviewActivity> activities) {
        return activities.stream()
                .filter(activity -> activity.hasReviewActivity())
                .filter(activity -> activity.hasHighCommentDensity() || activity.hasSignificantChangesAfterReview())
                .count();
    }

    private BooleanExpression activityDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return reviewActivity.createdAt.goe(startDate.atStartOfDay())
                    .and(reviewActivity.createdAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return reviewActivity.createdAt.goe(startDate.atStartOfDay());
        }

        return reviewActivity.createdAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private BooleanExpression sessionDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return reviewSession.createdAt.goe(startDate.atStartOfDay())
                    .and(reviewSession.createdAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return reviewSession.createdAt.goe(startDate.atStartOfDay());
        }

        return reviewSession.createdAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private BooleanExpression reviewDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return review.githubSubmittedAt.goe(startDate.atStartOfDay())
                    .and(review.githubSubmittedAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return review.githubSubmittedAt.goe(startDate.atStartOfDay());
        }

        return review.githubSubmittedAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private BooleanExpression responseTimeDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return reviewResponseTime.lastChangesRequestedAt.goe(startDate.atStartOfDay())
                    .and(reviewResponseTime.lastChangesRequestedAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return reviewResponseTime.lastChangesRequestedAt.goe(startDate.atStartOfDay());
        }

        return reviewResponseTime.lastChangesRequestedAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private Review selectEarlierReview(Review existing, Review replacement) {
        if (existing.getGithubSubmittedAt().isBefore(replacement.getGithubSubmittedAt())) {
            return existing;
        }
        return replacement;
    }

    private boolean isResolved(ReviewResponseTime responseTime) {
        return responseTime.isResolved();
    }
}
