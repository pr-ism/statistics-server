package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.ThroughputStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ThroughputStatisticsDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

@Repository
@RequiredArgsConstructor
public class ThroughputStatisticsRepositoryAdapter implements ThroughputStatisticsRepository {

    private static final long END_DATE_INCLUSIVE_DAYS = 1L;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<ThroughputStatisticsDto> findThroughputStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        BooleanExpression validMergedCondition = pullRequest.state.eq(PullRequestState.MERGED)
                .and(pullRequest.timing.githubCreatedAt.isNotNull())
                .and(pullRequest.timing.githubMergedAt.isNotNull());

        NumberExpression<Long> mergedCountExpression = new CaseBuilder()
                .when(validMergedCondition).then(1L)
                .otherwise(0L)
                .sumLong()
                .coalesce(0L);

        NumberExpression<Long> closedCountExpression = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.CLOSED)).then(1L)
                .otherwise(0L)
                .sumLong()
                .coalesce(0L);

        NumberExpression<Long> totalMergeTimeMinutesExpression = new CaseBuilder()
                .when(validMergedCondition)
                .then(Expressions.numberTemplate(
                        Long.class,
                        "TIMESTAMPDIFF(MINUTE, {0}, {1})",
                        pullRequest.timing.githubCreatedAt,
                        pullRequest.timing.githubMergedAt
                ))
                .otherwise(0L)
                .sumLong()
                .coalesce(0L);

        Tuple resultTuple = queryFactory
                .select(mergedCountExpression, closedCountExpression, totalMergeTimeMinutesExpression)
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .fetchOne();

        long mergedCount = resolveLong(resultTuple, mergedCountExpression);
        long closedCount = resolveLong(resultTuple, closedCountExpression);

        if (mergedCount == 0L && closedCount == 0L) {
            return Optional.empty();
        }

        long totalMergeTimeMinutes = resolveLong(resultTuple, totalMergeTimeMinutesExpression);

        return Optional.of(new ThroughputStatisticsDto(
                mergedCount,
                closedCount,
                totalMergeTimeMinutes
        ));
    }

    private long resolveLong(Tuple tuple, NumberExpression<Long> expression) {
        if (tuple == null) {
            return 0L;
        }
        Long value = tuple.get(expression);
        return value == null ? 0L : value;
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
