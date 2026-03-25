package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.review.QReviewResponseTime.reviewResponseTime;
import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;
import static com.prism.statistics.domain.analysis.metadata.review.QReview.review;

import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.statistics.repository.ReviewQualityStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewActivityStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSessionStatisticsDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewQualityStatisticsRepositoryAdapter implements ReviewQualityStatisticsRepository {

    private static final int HIGH_CHANGE_THRESHOLD = 10;
    private static final long DATE_RANGE_INCLUSIVE_DAYS = 1L;
    private static final BigDecimal HIGH_COMMENT_DENSITY_THRESHOLD = BigDecimal.valueOf(0.1);

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewActivityStatisticsDto> findReviewActivityStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ReviewActivityAggregate reviewActivityAggregate = aggregateReviewActivityMetricsByProjectId(
                projectId,
                startDate,
                endDate
        );

        if (reviewActivityAggregate.totalCount() == 0L) {
            return Optional.empty();
        }

        long changesRequestedCount = fetchChangesRequestedCount(projectId, startDate, endDate);
        long firstReviewApproveCount = fetchFirstReviewApproveCount(projectId, startDate, endDate);

        ChangesResolutionAggregate changesResolutionAggregate = aggregateChangesResolutionMetricsByProjectId(
                projectId,
                startDate,
                endDate
        );
        ReviewActivityStatisticsDto reviewActivityStatisticsDto = buildReviewActivityStatisticsDto(
                reviewActivityAggregate,
                firstReviewApproveCount,
                changesRequestedCount,
                changesResolutionAggregate
        );

        return Optional.of(reviewActivityStatisticsDto);
    }

    private ReviewActivityAggregate aggregateReviewActivityMetricsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        NumberExpression<Long> totalCountExpr = reviewActivity.count();
        NumberExpression<Long> reviewedCountExpr = new CaseBuilder()
                .when(reviewActivity.reviewRoundTrips.gt(0)).then(1L).otherwise(0L)
                .sumLong().coalesce(0L);
        NumberExpression<Long> totalReviewRoundTripsExpr = reviewActivity.reviewRoundTrips.sumLong().coalesce(0L);
        NumberExpression<Long> totalCommentCountExpr = reviewActivity.totalCommentCount.sumLong().coalesce(0L);
        NumberExpression<BigDecimal> totalCommentDensityExpr = reviewActivity.commentDensity.sumBigDecimal()
                                                                                      .coalesce(BigDecimal.ZERO);
        NumberExpression<Long> withAdditionalReviewersCountExpr = new CaseBuilder()
                .when(reviewActivity.hasAdditionalReviewers.isTrue()).then(1L).otherwise(0L)
                .sumLong().coalesce(0L);
        NumberExpression<Long> withChangesAfterReviewCountExpr = new CaseBuilder()
                .when(reviewActivity.codeAdditionsAfterReview.gt(0)
                                                             .or(reviewActivity.codeDeletionsAfterReview.gt(0)))
                .then(1L).otherwise(0L).sumLong().coalesce(0L);
        NumberExpression<Long> highIntensityPrCountExpr = new CaseBuilder()
                .when(reviewActivity.reviewRoundTrips.gt(0).and(
                        reviewActivity.commentDensity.goe(HIGH_COMMENT_DENSITY_THRESHOLD)
                                                     .or(reviewActivity.codeAdditionsAfterReview
                                                             .add(reviewActivity.codeDeletionsAfterReview)
                                                             .goe(HIGH_CHANGE_THRESHOLD))))
                .then(1L).otherwise(0L).sumLong().coalesce(0L);

        Tuple result = queryFactory
                .select(
                        totalCountExpr,
                        reviewedCountExpr,
                        totalReviewRoundTripsExpr,
                        totalCommentCountExpr,
                        totalCommentDensityExpr,
                        withAdditionalReviewersCountExpr,
                        withChangesAfterReviewCountExpr,
                        highIntensityPrCountExpr
                )
                .from(reviewActivity)
                .join(pullRequest).on(pullRequest.id.eq(reviewActivity.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(reviewActivity.createdAt, startDate, endDate)
                )
                .fetchOne();

        if (result == null) {
            return ReviewActivityAggregate.empty();
        }

        return new ReviewActivityAggregate(
                nullableLong(result, totalCountExpr),
                nullableLong(result, reviewedCountExpr),
                nullableLong(result, totalReviewRoundTripsExpr),
                nullableLong(result, totalCommentCountExpr),
                nullableBigDecimal(result, totalCommentDensityExpr),
                nullableLong(result, withAdditionalReviewersCountExpr),
                nullableLong(result, withChangesAfterReviewCountExpr),
                nullableLong(result, highIntensityPrCountExpr)
        );
    }

    private ChangesResolutionAggregate aggregateChangesResolutionMetricsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Tuple result = queryFactory
                .select(
                        reviewResponseTime.changesResolution.minutes.sumLong().coalesce(0L),
                        reviewResponseTime.changesResolution.minutes.count()
                )
                .from(reviewResponseTime)
                .join(pullRequest).on(pullRequest.id.eq(reviewResponseTime.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(reviewResponseTime.lastChangesRequestedAt, startDate, endDate)
                )
                .fetchOne();

        if (result == null) {
            return ChangesResolutionAggregate.empty();
        }

        return new ChangesResolutionAggregate(
                nullableLong(result, 0),
                nullableLong(result, 1)
        );
    }

    private ReviewActivityStatisticsDto buildReviewActivityStatisticsDto(
            ReviewActivityAggregate reviewActivityAggregate,
            long firstReviewApproveCount,
            long changesRequestedCount,
            ChangesResolutionAggregate changesResolutionAggregate
    ) {
        return new ReviewActivityStatisticsDto(
                reviewActivityAggregate.totalCount(),
                reviewActivityAggregate.reviewedCount(),
                reviewActivityAggregate.totalReviewRoundTrips(),
                reviewActivityAggregate.totalCommentCount(),
                reviewActivityAggregate.totalCommentDensity(),
                reviewActivityAggregate.withAdditionalReviewersCount(),
                reviewActivityAggregate.withChangesAfterReviewCount(),
                firstReviewApproveCount,
                changesRequestedCount,
                changesResolutionAggregate.totalChangesResolutionMinutes(),
                changesResolutionAggregate.changesResolvedCount(),
                reviewActivityAggregate.highIntensityPrCount()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewSessionStatisticsDto> findReviewSessionStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ReviewSessionAggregate reviewSessionAggregate = aggregateReviewSessionMetricsByProjectId(projectId, startDate, endDate);

        if (reviewSessionAggregate.totalSessionCount() == 0L) {
            return Optional.empty();
        }

        ReviewSessionStatisticsDto reviewSessionStatisticsDto = new ReviewSessionStatisticsDto(
                reviewSessionAggregate.totalSessionCount(),
                reviewSessionAggregate.uniqueReviewerCount(),
                reviewSessionAggregate.uniquePullRequestCount(),
                reviewSessionAggregate.totalSessionDurationMinutes(),
                reviewSessionAggregate.totalReviewCount()
        );

        return Optional.of(reviewSessionStatisticsDto);
    }

    private ReviewSessionAggregate aggregateReviewSessionMetricsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Tuple result = queryFactory
                .select(
                        reviewSession.count(),
                        reviewSession.reviewer.userId.countDistinct(),
                        reviewSession.pullRequestId.countDistinct(),
                        reviewSession.sessionDuration.minutes.sumLong().coalesce(0L),
                        reviewSession.reviewCount.sumLong().coalesce(0L)
                )
                .from(reviewSession)
                .join(pullRequest).on(pullRequest.id.eq(reviewSession.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(reviewSession.createdAt, startDate, endDate)
                )
                .fetchOne();

        if (result == null) {
            return ReviewSessionAggregate.empty();
        }

        return new ReviewSessionAggregate(
                nullableLong(result, 0),
                nullableLong(result, 1),
                nullableLong(result, 2),
                nullableLong(result, 3),
                nullableLong(result, 4)
        );
    }

    private long fetchChangesRequestedCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        Long count = queryFactory
                .select(review.pullRequestId.countDistinct())
                .from(review)
                .join(pullRequest).on(pullRequest.id.eq(review.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        review.reviewState.eq(ReviewState.CHANGES_REQUESTED),
                        dateRangeCondition(review.githubSubmittedAt, startDate, endDate)
                )
                .fetchOne();

        return Optional.ofNullable(count)
                       .orElse(0L);
    }

    private long fetchFirstReviewApproveCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        List<Tuple> firstReviewCandidates = queryFactory
                .select(
                        review.pullRequestId,
                        review.reviewState
                )
                .from(review)
                .join(pullRequest).on(pullRequest.id.eq(review.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(review.githubSubmittedAt, startDate, endDate)
                )
                .orderBy(
                        review.pullRequestId.asc(),
                        review.githubSubmittedAt.asc(),
                        review.id.asc()
                )
                .fetch();

        long approvedFirstReviewCount = 0L;
        Long previousPullRequestId = null;

        for (Tuple firstReviewCandidate : firstReviewCandidates) {
            Long pullRequestId = firstReviewCandidate.get(review.pullRequestId);
            ReviewState reviewState = firstReviewCandidate.get(review.reviewState);

            if (pullRequestId == null || pullRequestId.equals(previousPullRequestId)) {
                continue;
            }

            if (reviewState == ReviewState.APPROVED) {
                approvedFirstReviewCount++;
            }
            previousPullRequestId = pullRequestId;
        }

        return approvedFirstReviewCount;
    }

    private long nullableLong(Tuple tuple, int index) {
        Long value = tuple.get(index, Long.class);
        if (value != null) {
            return value;
        }

        return 0L;
    }

    private long nullableLong(Tuple tuple, Expression<Long> expression) {
        Long value = tuple.get(expression);
        if (value != null) {
            return value;
        }

        return 0L;
    }

    private BigDecimal nullableBigDecimal(Tuple tuple, Expression<BigDecimal> expression) {
        BigDecimal value = tuple.get(expression);
        if (value != null) {
            return value;
        }

        return BigDecimal.ZERO;
    }

    private BooleanExpression dateRangeCondition(
            DateTimePath<LocalDateTime> dateTimePath,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate != null && endDate != null) {
            return dateTimePath.goe(startDate.atStartOfDay())
                               .and(dateTimePath.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }
        if (startDate != null) {
            return dateTimePath.goe(startDate.atStartOfDay());
        }

        return dateTimePath.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private record ReviewActivityAggregate(
            long totalCount,
            long reviewedCount,
            long totalReviewRoundTrips,
            long totalCommentCount,
            BigDecimal totalCommentDensity,
            long withAdditionalReviewersCount,
            long withChangesAfterReviewCount,
            long highIntensityPrCount
    ) {
        static ReviewActivityAggregate empty() {
            return new ReviewActivityAggregate(0L, 0L, 0L, 0L, BigDecimal.ZERO, 0L, 0L, 0L);
        }
    }

    private record ChangesResolutionAggregate(
            long totalChangesResolutionMinutes,
            long changesResolvedCount
    ) {
        static ChangesResolutionAggregate empty() {
            return new ChangesResolutionAggregate(0L, 0L);
        }
    }

    private record ReviewSessionAggregate(
            long totalSessionCount,
            long uniqueReviewerCount,
            long uniquePullRequestCount,
            long totalSessionDurationMinutes,
            long totalReviewCount
    ) {
        static ReviewSessionAggregate empty() {
            return new ReviewSessionAggregate(0L, 0L, 0L, 0L, 0L);
        }
    }
}
