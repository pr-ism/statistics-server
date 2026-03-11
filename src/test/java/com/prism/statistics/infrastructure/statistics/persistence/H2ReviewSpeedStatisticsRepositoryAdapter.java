package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.statistics.repository.ReviewSpeedStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSpeedStatisticsDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

public class H2ReviewSpeedStatisticsRepositoryAdapter implements ReviewSpeedStatisticsRepository {

    private static final long DATE_RANGE_INCLUSIVE_DAYS = 1L;
    private static final int MINUTES_PER_HOUR = 60;

    private final JPAQueryFactory queryFactory;

    public H2ReviewSpeedStatisticsRepositoryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ReviewSpeedStatisticsDto> findReviewSpeedStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime coreTimeStart,
            LocalTime coreTimeEnd
    ) {
        Tuple result = queryFactory
                .select(
                        pullRequest.id.count(),
                        pullRequestBottleneck.firstReviewAt.count(),
                        pullRequestBottleneck.reviewWait.minutes.sumLong(),
                        mergedWithApprovalCountExpression(),
                        totalMergeWaitMinutesExpression(),
                        coreTimeReviewCountExpression(coreTimeStart, coreTimeEnd),
                        sameDayReviewCountExpression()
                )
                .from(pullRequest)
                .leftJoin(pullRequestBottleneck)
                .on(pullRequestBottleneck.pullRequestId.eq(pullRequest.id))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetchOne();

        long totalCount = result.get(0, Long.class);
        if (totalCount == 0L) {
            return Optional.empty();
        }

        List<Long> reviewWaitMinutesList = fetchReviewWaitMinutesList(projectId, startDate, endDate);

        return Optional.of(new ReviewSpeedStatisticsDto(
                totalCount,
                result.get(1, Long.class),
                nullToZero(result.get(2, Long.class)),
                reviewWaitMinutesList,
                nullToZero(result.get(3, Long.class)),
                nullToZero(result.get(4, Long.class)),
                nullToZero(result.get(5, Long.class)),
                nullToZero(result.get(6, Long.class))
        ));
    }

    private List<Long> fetchReviewWaitMinutesList(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return queryFactory
                .select(pullRequestBottleneck.reviewWait.minutes)
                .from(pullRequest)
                .join(pullRequestBottleneck)
                .on(pullRequestBottleneck.pullRequestId.eq(pullRequest.id))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate),
                        pullRequestBottleneck.firstReviewAt.isNotNull(),
                        pullRequestBottleneck.reviewWait.minutes.isNotNull()
                )
                .fetch();
    }

    private NumberExpression<Long> mergedWithApprovalCountExpression() {
        return new CaseBuilder()
                .when(pullRequestBottleneck.mergeWait.minutes.isNotNull()
                        .and(pullRequestBottleneck.lastApproveAt.isNotNull()))
                .then(1L)
                .otherwise(0L)
                .sumLong();
    }

    private NumberExpression<Long> totalMergeWaitMinutesExpression() {
        return new CaseBuilder()
                .when(pullRequestBottleneck.mergeWait.minutes.isNotNull()
                        .and(pullRequestBottleneck.lastApproveAt.isNotNull()))
                .then(pullRequestBottleneck.mergeWait.minutes)
                .otherwise(0L)
                .sumLong();
    }

    private NumberExpression<Long> coreTimeReviewCountExpression(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return Expressions.asNumber(0L);
        }

        NumberExpression<Integer> minuteOfDay = pullRequestBottleneck.firstReviewAt.hour()
                .multiply(MINUTES_PER_HOUR)
                .add(pullRequestBottleneck.firstReviewAt.minute());

        int startMinutes = start.getHour() * MINUTES_PER_HOUR + start.getMinute();
        int endMinutes = end.getHour() * MINUTES_PER_HOUR + end.getMinute();

        return new CaseBuilder()
                .when(pullRequestBottleneck.firstReviewAt.isNotNull()
                        .and(minuteOfDay.goe(startMinutes))
                        .and(minuteOfDay.loe(endMinutes)))
                .then(1L)
                .otherwise(0L)
                .sumLong();
    }

    private NumberExpression<Long> sameDayReviewCountExpression() {
        DateTemplate<Date> createdAtDate = toDate(pullRequest.timing.githubCreatedAt);
        DateTemplate<Date> firstReviewDate = toDate(pullRequestBottleneck.firstReviewAt);

        return new CaseBuilder()
                .when(pullRequestBottleneck.firstReviewAt.isNotNull()
                        .and(pullRequest.timing.githubCreatedAt.isNotNull())
                        .and(createdAtDate.eq(firstReviewDate)))
                .then(1L)
                .otherwise(0L)
                .sumLong();
    }

    private DateTemplate<Date> toDate(com.querydsl.core.types.dsl.DateTimeExpression<?> dateTime) {
        return Expressions.dateTemplate(Date.class, "cast({0} as date)", dateTime);
    }

    private long nullToZero(Long value) {
        if (value == null) {
            return 0L;
        }
        return value;
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay())
                    .and(pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }
}
