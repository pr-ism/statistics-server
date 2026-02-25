package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.DailyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto.DailyPrCountDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

@Repository
@RequiredArgsConstructor
public class DailyTrendStatisticsRepositoryAdapter implements DailyTrendStatisticsRepository {

    private static final long END_DATE_INCLUSIVE_DAYS = 1L;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyTrendStatisticsDto> findDailyTrendStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<DailyPrCountDto> dailyCreatedCounts = aggregateDailyCreatedCountsFromDb(projectId, startDate, endDate);
        List<DailyPrCountDto> dailyMergedCounts = aggregateDailyMergedCountsFromDb(projectId, startDate, endDate);

        if (dailyCreatedCounts.isEmpty() && dailyMergedCounts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new DailyTrendStatisticsDto(dailyCreatedCounts, dailyMergedCounts));
    }

    private List<DailyPrCountDto> aggregateDailyCreatedCountsFromDb(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        DateTemplate<Date> createdDate = Expressions.dateTemplate(
                Date.class,
                "cast({0} as date)",
                pullRequest.timing.githubCreatedAt
        );

        List<Tuple> rows = queryFactory
                .select(createdDate, pullRequest.count())
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.timing.githubCreatedAt.isNotNull(),
                        createdAtDateRangeCondition(startDate, endDate)
                )
                .groupBy(createdDate)
                .fetch();

        return rows.stream()
                .map(row -> new DailyPrCountDto(resolveLocalDate(row.get(createdDate)), row.get(pullRequest.count())))
                .sorted(Comparator.comparing((DailyPrCountDto item) -> item.date()))
                .toList();
    }

    private LocalDate resolveLocalDate(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    private List<DailyPrCountDto> aggregateDailyMergedCountsFromDb(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        DateTemplate<Date> mergedDate = Expressions.dateTemplate(
                Date.class,
                "cast({0} as date)",
                pullRequest.timing.githubMergedAt
        );

        List<Tuple> rows = queryFactory
                .select(mergedDate, pullRequest.count())
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.eq(PullRequestState.MERGED),
                        pullRequest.timing.githubMergedAt.isNotNull(),
                        mergedAtDateRangeCondition(startDate, endDate)
                )
                .groupBy(mergedDate)
                .fetch();

        return rows.stream()
                .map(row -> new DailyPrCountDto(resolveLocalDate(row.get(mergedDate)), row.get(pullRequest.count())))
                .sorted(Comparator.comparing((DailyPrCountDto item) -> item.date()))
                .toList();
    }

    private BooleanExpression createdAtDateRangeCondition(LocalDate startDate, LocalDate endDate) {
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

    private BooleanExpression mergedAtDateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.timing.githubMergedAt.goe(startDate.atStartOfDay())
                    .and(pullRequest.timing.githubMergedAt.lt(endDate.plusDays(END_DATE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.timing.githubMergedAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.timing.githubMergedAt.lt(endDate.plusDays(END_DATE_INCLUSIVE_DAYS).atStartOfDay());
    }
}
