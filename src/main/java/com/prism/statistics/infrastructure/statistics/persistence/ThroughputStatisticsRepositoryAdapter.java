package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.ThroughputStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ThroughputStatisticsDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        NumberExpression<Long> mergedCountExpression = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.MERGED)).then(1L)
                .otherwise(0L)
                .sumLong()
                .coalesce(0L);

        NumberExpression<Long> closedCountExpression = new CaseBuilder()
                .when(pullRequest.state.eq(PullRequestState.CLOSED)).then(1L)
                .otherwise(0L)
                .sumLong()
                .coalesce(0L);

        Tuple countTuple = queryFactory
                .select(mergedCountExpression, closedCountExpression)
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .fetchOne();

        long mergedCount = resolveLong(countTuple, mergedCountExpression);
        long closedCount = resolveLong(countTuple, closedCountExpression);

        if (mergedCount == 0L && closedCount == 0L) {
            return Optional.empty();
        }

        List<Tuple> mergedTimings = queryFactory
                .select(
                        pullRequest.timing.githubCreatedAt,
                        pullRequest.timing.githubMergedAt
                )
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.eq(PullRequestState.MERGED),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .fetch();

        long totalMergeTimeMinutes = mergedTimings.stream()
                .mapToLong(row -> calculateMergeMinutes(
                        row.get(pullRequest.timing.githubCreatedAt),
                        row.get(pullRequest.timing.githubMergedAt)
                ))
                .sum();

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

    private long calculateMergeMinutes(LocalDateTime createdAt, LocalDateTime mergedAt) {
        if (createdAt == null || mergedAt == null) {
            return 0L;
        }

        return Duration.between(createdAt, mergedAt).toMinutes();
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
