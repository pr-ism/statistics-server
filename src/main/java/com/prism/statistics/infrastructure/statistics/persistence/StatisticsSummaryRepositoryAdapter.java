package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.insight.lifecycle.QPullRequestLifecycle.pullRequestLifecycle;
import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;
import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.StatisticsSummaryRepository;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.BottleneckDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.OverviewDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.ReviewHealthDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.TeamActivityDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
public class StatisticsSummaryRepositoryAdapter implements StatisticsSummaryRepository {

    private static final String NOT_AVAILABLE_GRADE = "N/A";

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<StatisticsSummaryDto> findStatisticsSummaryByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        long totalPrCount = fetchTotalPrCount(projectId, startDate, endDate);
        if (totalPrCount == 0L) {
            return Optional.empty();
        }

        OverviewDto overview = buildOverviewDto(projectId, startDate, endDate, totalPrCount);
        ReviewActivityAggregate reviewActivityAggregate = fetchReviewActivityAggregate(projectId, startDate, endDate);
        BottleneckAggregate bottleneckAggregate = fetchBottleneckAggregate(projectId, startDate, endDate);

        ReviewHealthDto reviewHealth = buildReviewHealthDto(
                projectId,
                startDate,
                endDate,
                totalPrCount,
                reviewActivityAggregate,
                bottleneckAggregate
        );
        TeamActivityDto teamActivity = buildTeamActivityDto(
                projectId,
                startDate,
                endDate,
                reviewActivityAggregate
        );
        BottleneckDto bottleneck = buildBottleneckDto(bottleneckAggregate);

        return Optional.of(new StatisticsSummaryDto(overview, reviewHealth, teamActivity, bottleneck));
    }

    private long fetchTotalPrCount(Long projectId, LocalDate startDate, LocalDate endDate) {
        Long totalPrCount = queryFactory
                .select(pullRequest.count())
                .from(pullRequest)
                .where(pullRequestScopeCondition(projectId, startDate, endDate))
                .fetchOne();
        return totalPrCount != null ? totalPrCount : 0L;
    }

    private OverviewDto buildOverviewDto(Long projectId, LocalDate startDate, LocalDate endDate, long totalPrCount) {
        Tuple closedAggregate = fetchClosedOverviewAggregate(projectId, startDate, endDate);
        long mergedPrCount = nullableLong(closedAggregate, 0);
        long closedPrCount = nullableLong(closedAggregate, 1);
        long totalMergeTimeMinutes = nullableLong(closedAggregate, 2);

        SizeAggregate sizeAggregate = fetchSizeAggregate(projectId, startDate, endDate);

        return new OverviewDto(
                totalPrCount,
                mergedPrCount,
                closedPrCount,
                totalMergeTimeMinutes,
                sizeAggregate.totalSizeScore(),
                sizeAggregate.sizeMeasuredCount(),
                sizeAggregate.dominantSizeGrade()
        );
    }

    private Tuple fetchClosedOverviewAggregate(Long projectId, LocalDate startDate, LocalDate endDate) {
        NumberExpression<Long> mergeMinutesExpression = new CaseBuilder()
                .when(
                        pullRequest.state.eq(PullRequestState.MERGED)
                                         .and(pullRequest.timing.githubCreatedAt.isNotNull())
                                         .and(pullRequest.timing.githubMergedAt.isNotNull())
                )
                .then(Expressions.numberTemplate(
                        Long.class,
                        "timestampdiff(minute, {0}, {1})",
                        pullRequest.timing.githubCreatedAt,
                        pullRequest.timing.githubMergedAt
                ))
                .otherwise(0L);

        return queryFactory
                .select(
                        new CaseBuilder()
                                .when(pullRequest.state.eq(PullRequestState.MERGED)).then(1L)
                                .otherwise(0L).sumLong().coalesce(0L),
                        new CaseBuilder()
                                .when(pullRequest.state.eq(PullRequestState.CLOSED)).then(1L)
                                .otherwise(0L).sumLong().coalesce(0L),
                        mergeMinutesExpression.sumLong().coalesce(0L)
                )
                .from(pullRequest)
                .where(
                        pullRequestScopeCondition(projectId, startDate, endDate),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .fetchOne();
    }

    private SizeAggregate fetchSizeAggregate(Long projectId, LocalDate startDate, LocalDate endDate) {
        Tuple summary = queryFactory
                .select(
                        pullRequestSize.sizeScore.sumBigDecimal().coalesce(BigDecimal.ZERO),
                        pullRequestSize.count()
                )
                .from(pullRequestSize)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestSize.pullRequestId))
                .where(pullRequestScopeCondition(projectId, startDate, endDate))
                .fetchOne();

        BigDecimal totalSizeScore = summary != null
                ? summary.get(0, BigDecimal.class)
                : BigDecimal.ZERO;
        long sizeMeasuredCount = nullableLong(summary, 1);

        Tuple dominantGrade = queryFactory
                .select(pullRequestSize.sizeGrade, pullRequestSize.count())
                .from(pullRequestSize)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestSize.pullRequestId))
                .where(pullRequestScopeCondition(projectId, startDate, endDate))
                .groupBy(pullRequestSize.sizeGrade)
                .orderBy(pullRequestSize.count().desc(), pullRequestSize.sizeGrade.asc())
                .limit(1)
                .fetchOne();

        String dominantSizeGrade = Optional.ofNullable(dominantGrade)
                                           .map(tuple -> tuple.get(pullRequestSize.sizeGrade))
                                           .map(SizeGrade::name)
                                           .orElse(NOT_AVAILABLE_GRADE);

        return new SizeAggregate(totalSizeScore != null ? totalSizeScore : BigDecimal.ZERO, sizeMeasuredCount, dominantSizeGrade);
    }

    private ReviewActivityAggregate fetchReviewActivityAggregate(Long projectId, LocalDate startDate, LocalDate endDate) {
        Tuple aggregate = queryFactory
                .select(
                        new CaseBuilder()
                                .when(reviewActivity.totalCommentCount.gt(0).or(reviewActivity.reviewRoundTrips.gt(0))).then(1L)
                                .otherwise(0L).sumLong().coalesce(0L),
                        new CaseBuilder()
                                .when(reviewActivity.reviewRoundTrips.eq(1).and(reviewActivity.hasAdditionalReviewers.isFalse())).then(1L)
                                .otherwise(0L).sumLong().coalesce(0L),
                        new CaseBuilder()
                                .when(reviewActivity.codeAdditionsAfterReview.gt(0).or(reviewActivity.codeDeletionsAfterReview.gt(0))).then(1L)
                                .otherwise(0L).sumLong().coalesce(0L),
                        reviewActivity.reviewRoundTrips.sumLong().coalesce(0L),
                        reviewActivity.totalCommentCount.sumLong().coalesce(0L)
                )
                .from(reviewActivity)
                .join(pullRequest).on(pullRequest.id.eq(reviewActivity.pullRequestId))
                .where(pullRequestScopeCondition(projectId, startDate, endDate))
                .fetchOne();

        return new ReviewActivityAggregate(
                nullableLong(aggregate, 0),
                nullableLong(aggregate, 1),
                nullableLong(aggregate, 2),
                nullableLong(aggregate, 3),
                nullableLong(aggregate, 4)
        );
    }

    private ReviewHealthDto buildReviewHealthDto(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            long totalPrCount,
            ReviewActivityAggregate reviewActivityAggregate,
            BottleneckAggregate bottleneckAggregate
    ) {
        Long closedWithoutReviewCount = queryFactory
                .select(pullRequestLifecycle.count())
                .from(pullRequestLifecycle)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestLifecycle.pullRequestId))
                .where(
                        pullRequestScopeCondition(projectId, startDate, endDate),
                        pullRequestLifecycle.closedWithoutReview.isTrue()
                )
                .fetchOne();

        return new ReviewHealthDto(
                totalPrCount,
                reviewActivityAggregate.reviewedPrCount(),
                bottleneckAggregate.totalReviewWaitMinutes(),
                reviewActivityAggregate.firstReviewApproveCount(),
                reviewActivityAggregate.changesRequestedCount(),
                closedWithoutReviewCount != null ? closedWithoutReviewCount : 0L
        );
    }

    private TeamActivityDto buildTeamActivityDto(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            ReviewActivityAggregate reviewActivityAggregate
    ) {
        Long uniquePullRequestCountResult = queryFactory
                .select(pullRequest.count())
                .from(pullRequest)
                .where(
                        pullRequestScopeCondition(projectId, startDate, endDate),
                        JPAExpressions
                                .selectOne()
                                .from(reviewSession)
                                .where(reviewSession.pullRequestId.eq(pullRequest.id))
                                .exists()
                )
                .fetchOne();
        long uniquePullRequestCount = uniquePullRequestCountResult != null ? uniquePullRequestCountResult : 0L;

        List<Long> reviewerAssignmentCounts = queryFactory
                .select(reviewSession.count())
                .from(reviewSession)
                .where(
                        JPAExpressions
                                .selectOne()
                                .from(pullRequest)
                                .where(
                                        pullRequest.id.eq(reviewSession.pullRequestId),
                                        pullRequestScopeCondition(projectId, startDate, endDate)
                                )
                                .exists()
                )
                .groupBy(reviewSession.reviewer.userId)
                .fetch();

        long uniqueReviewerCount = reviewerAssignmentCounts.size();
        long totalReviewerAssignments = reviewerAssignmentCounts.stream()
                .mapToLong(Long::longValue)
                .sum();

        double giniCoefficient = calculateGiniCoefficient(reviewerAssignmentCounts);

        return new TeamActivityDto(
                uniqueReviewerCount,
                uniquePullRequestCount,
                totalReviewerAssignments,
                reviewActivityAggregate.totalReviewRoundTrips(),
                reviewActivityAggregate.totalCommentCount(),
                giniCoefficient
        );
    }

    private double calculateGiniCoefficient(List<Long> reviewerAssignmentCounts) {
        if (reviewerAssignmentCounts.isEmpty()) {
            return 0.0;
        }

        List<Long> counts = reviewerAssignmentCounts.stream()
                                                    .sorted()
                                                    .toList();

        int n = counts.size();
        if (n <= 1) {
            return 0.0;
        }

        double totalSum = counts.stream()
                                .mapToDouble(Long::doubleValue)
                                .sum();
        if (totalSum == 0.0) {
            return 0.0;
        }

        double giniSum = 0.0;
        for (int i = 0; i < n; i++) {
            giniSum += (2.0 * (i + 1) - n - 1) * counts.get(i);
        }

        return giniSum / (n * totalSum);
    }

    private BottleneckAggregate fetchBottleneckAggregate(Long projectId, LocalDate startDate, LocalDate endDate) {
        Tuple aggregate = queryFactory
                .select(
                        pullRequestBottleneck.reviewWait.minutes.sumLong().coalesce(0L),
                        pullRequestBottleneck.reviewProgress.minutes.sumLong().coalesce(0L),
                        pullRequestBottleneck.mergeWait.minutes.sumLong().coalesce(0L),
                        pullRequestBottleneck.count()
                )
                .from(pullRequest)
                .join(pullRequestBottleneck).on(pullRequest.id.eq(pullRequestBottleneck.pullRequestId))
                .where(pullRequestScopeCondition(projectId, startDate, endDate))
                .fetchOne();

        return new BottleneckAggregate(
                nullableLong(aggregate, 0),
                nullableLong(aggregate, 1),
                nullableLong(aggregate, 2),
                nullableLong(aggregate, 3)
        );
    }

    private BottleneckDto buildBottleneckDto(BottleneckAggregate bottleneckAggregate) {
        return new BottleneckDto(
                bottleneckAggregate.totalReviewWaitMinutes(),
                bottleneckAggregate.totalReviewProgressMinutes(),
                bottleneckAggregate.totalMergeWaitMinutes(),
                bottleneckAggregate.bottleneckCount()
        );
    }

    private long nullableLong(Tuple tuple, int index) {
        if (tuple == null) {
            return 0L;
        }
        Number value = tuple.get(index, Number.class);
        return value != null ? value.longValue() : 0L;
    }

    private BooleanExpression pullRequestScopeCondition(Long projectId, LocalDate startDate, LocalDate endDate) {
        return pullRequest.projectId.eq(projectId)
                                    .and(createdAtDateRangeCondition(startDate, endDate));
    }

    private BooleanExpression createdAtDateRangeCondition(LocalDate startDate, LocalDate endDate) {
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

    private BooleanExpression closedAtDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.timing.githubClosedAt.goe(startDate.atStartOfDay())
                                                    .and(pullRequest.timing.githubClosedAt.lt(endDate.plusDays(1).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.timing.githubClosedAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.timing.githubClosedAt.lt(endDate.plusDays(1).atStartOfDay());
    }

    private record SizeAggregate(
            BigDecimal totalSizeScore,
            long sizeMeasuredCount,
            String dominantSizeGrade
    ) {
    }

    private record ReviewActivityAggregate(
            long reviewedPrCount,
            long firstReviewApproveCount,
            long changesRequestedCount,
            long totalReviewRoundTrips,
            long totalCommentCount
    ) {
    }

    private record BottleneckAggregate(
            long totalReviewWaitMinutes,
            long totalReviewProgressMinutes,
            long totalMergeWaitMinutes,
            long bottleneckCount
    ) {
    }
}
