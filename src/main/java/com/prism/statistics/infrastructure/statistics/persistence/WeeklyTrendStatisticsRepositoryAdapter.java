package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.WeeklyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.MonthlyThroughputDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.WeeklyPrSizeDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.WeeklyReviewWaitTimeDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.WeeklyThroughputDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.Ops;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

@Repository
@RequiredArgsConstructor
public class WeeklyTrendStatisticsRepositoryAdapter implements WeeklyTrendStatisticsRepository {

    private static final long END_DATE_INCLUSIVE_DAYS = 1L;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<WeeklyTrendStatisticsDto> findWeeklyTrendStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<WeeklyThroughputDto> weeklyThroughputs = fetchWeeklyThroughput(projectId, startDate, endDate);
        List<MonthlyThroughputDto> monthlyThroughputs = fetchMonthlyThroughput(projectId, startDate, endDate);
        CreatedSideMetrics createdSideMetrics = fetchCreatedSideMetrics(projectId, startDate, endDate);

        if (weeklyThroughputs.isEmpty() && monthlyThroughputs.isEmpty()
                && createdSideMetrics.weeklyReviewWaitTimes().isEmpty()
                && createdSideMetrics.weeklyPrSizes().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new WeeklyTrendStatisticsDto(
                weeklyThroughputs,
                monthlyThroughputs,
                createdSideMetrics.weeklyReviewWaitTimes(),
                createdSideMetrics.weeklyPrSizes()
        ));
    }

    private List<WeeklyThroughputDto> fetchWeeklyThroughput(
            Long projectId, LocalDate startDate, LocalDate endDate
    ) {
        DateExpression<LocalDate> weekStartDate = weekStartDateOf(pullRequest.timing.githubClosedAt);

        NumberExpression<Long> mergedCount = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.MERGED)).then(1L)
                .otherwise(0L)
                .sumLong();

        NumberExpression<Long> closedCount = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.CLOSED)).then(1L)
                .otherwise(0L)
                .sumLong();

        return queryFactory
                .select(weekStartDate, mergedCount, closedCount)
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        pullRequest.timing.githubClosedAt.isNotNull(),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .groupBy(weekStartDate)
                .orderBy(weekStartDate.asc())
                .fetch()
                .stream()
                .map(tuple -> new WeeklyThroughputDto(
                        tuple.get(weekStartDate),
                        tuple.get(mergedCount),
                        tuple.get(closedCount)
                ))
                .toList();
    }

    private List<MonthlyThroughputDto> fetchMonthlyThroughput(
            Long projectId, LocalDate startDate, LocalDate endDate
    ) {
        NumberExpression<Integer> yearExpr = pullRequest.timing.githubClosedAt.year();
        NumberExpression<Integer> monthExpr = pullRequest.timing.githubClosedAt.month();

        NumberExpression<Long> mergedCount = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.MERGED)).then(1L)
                .otherwise(0L)
                .sumLong();

        NumberExpression<Long> closedCount = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.CLOSED)).then(1L)
                .otherwise(0L)
                .sumLong();

        return queryFactory
                .select(yearExpr, monthExpr, mergedCount, closedCount)
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        pullRequest.timing.githubClosedAt.isNotNull(),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .groupBy(yearExpr, monthExpr)
                .orderBy(yearExpr.asc(), monthExpr.asc())
                .fetch()
                .stream()
                .map(tuple -> new MonthlyThroughputDto(
                        tuple.get(yearExpr),
                        tuple.get(monthExpr),
                        tuple.get(mergedCount),
                        tuple.get(closedCount)
                ))
                .toList();
    }

    private CreatedSideMetrics fetchCreatedSideMetrics(
            Long projectId, LocalDate startDate, LocalDate endDate
    ) {
        DateExpression<LocalDate> weekStartDate = weekStartDateOf(pullRequest.timing.githubCreatedAt);
        NumberExpression<Double> avgReviewWait = pullRequestBottleneck.reviewWait.minutes.avg();
        NumberExpression<Double> avgSizeScore = pullRequestSize.sizeScore.avg();

        List<Tuple> tuples = queryFactory
                .select(weekStartDate, avgReviewWait, avgSizeScore)
                .from(pullRequest)
                .leftJoin(pullRequestBottleneck)
                .on(pullRequestBottleneck.pullRequestId.eq(pullRequest.id)
                        .and(pullRequestBottleneck.firstReviewAt.isNotNull())
                        .and(pullRequestBottleneck.reviewWait.minutes.isNotNull()))
                .leftJoin(pullRequestSize)
                .on(pullRequestSize.pullRequestId.eq(pullRequest.id))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .groupBy(weekStartDate)
                .orderBy(weekStartDate.asc())
                .fetch();

        List<WeeklyReviewWaitTimeDto> reviewWaitTimes = tuples.stream()
                .filter(tuple -> tuple.get(avgReviewWait) != null)
                .map(tuple -> new WeeklyReviewWaitTimeDto(
                        tuple.get(weekStartDate),
                        tuple.get(avgReviewWait)
                ))
                .toList();

        List<WeeklyPrSizeDto> prSizes = tuples.stream()
                .filter(tuple -> tuple.get(avgSizeScore) != null)
                .map(tuple -> new WeeklyPrSizeDto(
                        tuple.get(weekStartDate),
                        tuple.get(avgSizeScore)
                ))
                .toList();

        return new CreatedSideMetrics(reviewWaitTimes, prSizes);
    }

    private record CreatedSideMetrics(
            List<WeeklyReviewWaitTimeDto> weeklyReviewWaitTimes,
            List<WeeklyPrSizeDto> weeklyPrSizes
    ) {
    }

    private DateExpression<LocalDate> weekStartDateOf(DateTimePath<LocalDateTime> dateTimePath) {
        NumberExpression<Integer> mondayOffset = dateTimePath.dayOfWeek().add(5).mod(7);
        DateTimeExpression<LocalDateTime> mondayDateTime = Expressions.dateTimeOperation(
                LocalDateTime.class,
                Ops.DateTimeOps.ADD_DAYS,
                dateTimePath,
                mondayOffset.negate()
        );

        return Expressions.dateOperation(
                LocalDate.class,
                Ops.DateTimeOps.DATE,
                mondayDateTime
        );
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay())
                    .and(pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(END_DATE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(END_DATE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private BooleanExpression closedAtDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.timing.githubClosedAt.goe(startDate.atStartOfDay())
                    .and(pullRequest.timing.githubClosedAt.lt(endDate.plusDays(END_DATE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.timing.githubClosedAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.timing.githubClosedAt.lt(endDate.plusDays(END_DATE_INCLUSIVE_DAYS).atStartOfDay());
    }
}
