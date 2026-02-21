package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.statistics.repository.ReviewQualityStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewActivityStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSessionStatisticsDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewQualityStatisticsRepositoryAdapter implements ReviewQualityStatisticsRepository {

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

        return Optional.of(aggregateActivityStatistics(activities));
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

    private ReviewActivityStatisticsDto aggregateActivityStatistics(List<ReviewActivity> activities) {
        long totalCount = activities.size();

        long reviewedCount = activities.stream()
                .filter(ReviewActivity::hasReviewActivity)
                .count();

        long totalReviewRoundTrips = activities.stream()
                .mapToInt(ReviewActivity::getReviewRoundTrips)
                .sum();

        long totalCommentCount = activities.stream()
                .mapToInt(ReviewActivity::getTotalCommentCount)
                .sum();

        BigDecimal totalCommentDensity = activities.stream()
                .map(ReviewActivity::getCommentDensity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long withAdditionalReviewersCount = activities.stream()
                .filter(ReviewActivity::isHasAdditionalReviewers)
                .count();

        long withChangesAfterReviewCount = activities.stream()
                .filter(ReviewActivity::hasSignificantChangesAfterReview)
                .count();

        return new ReviewActivityStatisticsDto(
                totalCount,
                reviewedCount,
                totalReviewRoundTrips,
                totalCommentCount,
                totalCommentDensity,
                withAdditionalReviewersCount,
                withChangesAfterReviewCount
        );
    }

    private ReviewSessionStatisticsDto aggregateSessionStatistics(List<ReviewSession> sessions) {
        long totalSessionCount = sessions.size();

        long uniqueReviewerCount = sessions.stream()
                .map(s -> s.getReviewer().getUserId())
                .distinct()
                .count();

        long uniquePullRequestCount = sessions.stream()
                .map(ReviewSession::getPullRequestId)
                .distinct()
                .count();

        long totalSessionDurationMinutes = sessions.stream()
                .filter(s -> s.getSessionDuration() != null)
                .mapToLong(s -> s.getSessionDuration().getMinutes())
                .sum();

        long totalReviewCount = sessions.stream()
                .mapToInt(ReviewSession::getReviewCount)
                .sum();

        return new ReviewSessionStatisticsDto(
                totalSessionCount,
                uniqueReviewerCount,
                uniquePullRequestCount,
                totalSessionDurationMinutes,
                totalReviewCount
        );
    }

    private BooleanExpression activityDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return reviewActivity.createdAt.goe(startDate.atStartOfDay())
                    .and(reviewActivity.createdAt.lt(endDate.plusDays(1).atStartOfDay()));
        }

        if (startDate != null) {
            return reviewActivity.createdAt.goe(startDate.atStartOfDay());
        }

        return reviewActivity.createdAt.lt(endDate.plusDays(1).atStartOfDay());
    }

    private BooleanExpression sessionDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return reviewSession.createdAt.between(
                    startDate.atStartOfDay(),
                    endDate.plusDays(1).atStartOfDay()
            );
        }

        if (startDate != null) {
            return reviewSession.createdAt.goe(startDate.atStartOfDay());
        }

        return reviewSession.createdAt.lt(endDate.plusDays(1).atStartOfDay());
    }
}
