package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.review.QReviewResponseTime.reviewResponseTime;
import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;
import static com.prism.statistics.domain.analysis.metadata.review.QReview.review;

import com.prism.statistics.domain.analysis.metadata.review.QReview;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.statistics.repository.ReviewQualityStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewActivityStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSessionStatisticsDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        ReviewActivityAggregate result = queryFactory
                .select(Projections.constructor(ReviewActivityAggregate.class,
                        reviewActivity.count(),
                        new CaseBuilder()
                                .when(reviewActivity.reviewRoundTrips.gt(0)).then(1L).otherwise(0L)
                                .sumLong().coalesce(0L),
                        reviewActivity.reviewRoundTrips.sumLong().coalesce(0L),
                        reviewActivity.totalCommentCount.sumLong().coalesce(0L),
                        reviewActivity.commentDensity.sumBigDecimal().coalesce(BigDecimal.ZERO),
                        new CaseBuilder()
                                .when(reviewActivity.hasAdditionalReviewers.isTrue()).then(1L).otherwise(0L)
                                .sumLong().coalesce(0L),
                        new CaseBuilder()
                                .when(reviewActivity.codeAdditionsAfterReview.gt(0)
                                        .or(reviewActivity.codeDeletionsAfterReview.gt(0)))
                                .then(1L).otherwise(0L).sumLong().coalesce(0L),
                        new CaseBuilder()
                                .when(reviewActivity.reviewRoundTrips.gt(0).and(
                                        reviewActivity.commentDensity.goe(HIGH_COMMENT_DENSITY_THRESHOLD)
                                                .or(reviewActivity.codeAdditionsAfterReview
                                                        .add(reviewActivity.codeDeletionsAfterReview)
                                                        .goe(HIGH_CHANGE_THRESHOLD))))
                                .then(1L).otherwise(0L).sumLong().coalesce(0L)
                ))
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

        return result;
    }

    private ChangesResolutionAggregate aggregateChangesResolutionMetricsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ChangesResolutionAggregate result = queryFactory
                .select(Projections.constructor(ChangesResolutionAggregate.class,
                        reviewResponseTime.changesResolution.minutes.sumLong().coalesce(0L),
                        reviewResponseTime.changesResolution.minutes.count()
                ))
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

        return result;
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
        ReviewSessionAggregate result = queryFactory
                .select(Projections.constructor(ReviewSessionAggregate.class,
                        reviewSession.count(),
                        reviewSession.reviewer.userId.countDistinct(),
                        reviewSession.pullRequestId.countDistinct(),
                        reviewSession.sessionDuration.minutes.sumLong().coalesce(0L),
                        reviewSession.reviewCount.sumLong().coalesce(0L)
                ))
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

        return result;
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
        QReview reviewSub = new QReview("reviewSub");

        Long count = queryFactory
                .select(review.count())
                .from(review)
                .join(pullRequest).on(pullRequest.id.eq(review.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        review.reviewState.eq(ReviewState.APPROVED),
                        dateRangeCondition(review.githubSubmittedAt, startDate, endDate),
                        review.githubSubmittedAt.eq(
                                JPAExpressions.select(reviewSub.githubSubmittedAt.min())
                                        .from(reviewSub)
                                        .where(
                                                reviewSub.pullRequestId.eq(review.pullRequestId),
                                                dateRangeCondition(reviewSub.githubSubmittedAt, startDate, endDate)
                                        )
                        )
                )
                .fetchOne();

        return Optional.ofNullable(count)
                       .orElse(0L);
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
            Long totalCount,
            Long reviewedCount,
            Long totalReviewRoundTrips,
            Long totalCommentCount,
            BigDecimal totalCommentDensity,
            Long withAdditionalReviewersCount,
            Long withChangesAfterReviewCount,
            Long highIntensityPrCount
    ) {
        static ReviewActivityAggregate empty() {
            return new ReviewActivityAggregate(0L, 0L, 0L, 0L, BigDecimal.ZERO, 0L, 0L, 0L);
        }
    }

    private record ChangesResolutionAggregate(
            Long totalChangesResolutionMinutes,
            Long changesResolvedCount
    ) {
        static ChangesResolutionAggregate empty() {
            return new ChangesResolutionAggregate(0L, 0L);
        }
    }

    private record ReviewSessionAggregate(
            Long totalSessionCount,
            Long uniqueReviewerCount,
            Long uniquePullRequestCount,
            Long totalSessionDurationMinutes,
            Long totalReviewCount
    ) {
        static ReviewSessionAggregate empty() {
            return new ReviewSessionAggregate(0L, 0L, 0L, 0L, 0L);
        }
    }
}
