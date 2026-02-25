package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.WeeklyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.MonthlyThroughputDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.WeeklyPrSizeDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.WeeklyReviewWaitTimeDto;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto.WeeklyThroughputDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

@Repository
@RequiredArgsConstructor
public class WeeklyTrendStatisticsRepositoryAdapter implements WeeklyTrendStatisticsRepository {

    private static final long END_DATE_INCLUSIVE_DAYS = 1L;
    private static final double ZERO_DOUBLE = 0.0;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<WeeklyTrendStatisticsDto> findWeeklyTrendStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequest> pullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (pullRequests.isEmpty()) {
            return Optional.empty();
        }

        List<Long> pullRequestIds = pullRequests.stream()
                .map(pr -> pr.getId())
                .toList();

        Map<Long, PullRequestBottleneck> bottleneckMap = queryFactory
                .selectFrom(pullRequestBottleneck)
                .where(pullRequestBottleneck.pullRequestId.in(pullRequestIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(bottleneck -> bottleneck.getPullRequestId(), bottleneck -> bottleneck));

        Map<Long, PullRequestSize> sizeMap = queryFactory
                .selectFrom(pullRequestSize)
                .where(pullRequestSize.pullRequestId.in(pullRequestIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(size -> size.getPullRequestId(), size -> size));

        List<WeeklyThroughputDto> weeklyThroughputs = aggregateWeeklyThroughput(pullRequests, startDate, endDate);
        List<MonthlyThroughputDto> monthlyThroughputs = aggregateMonthlyThroughput(pullRequests, startDate, endDate);
        List<WeeklyReviewWaitTimeDto> weeklyReviewWaitTimes = aggregateWeeklyReviewWaitTime(pullRequests, bottleneckMap);
        List<WeeklyPrSizeDto> weeklyPrSizes = aggregateWeeklyPrSize(pullRequests, sizeMap);

        return Optional.of(new WeeklyTrendStatisticsDto(
                weeklyThroughputs,
                monthlyThroughputs,
                weeklyReviewWaitTimes,
                weeklyPrSizes
        ));
    }

    private List<WeeklyThroughputDto> aggregateWeeklyThroughput(
            List<PullRequest> pullRequests,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<LocalDate, List<PullRequest>> prsByWeek = pullRequests.stream()
                .filter(pr -> pr.getState() == PullRequestState.MERGED || pr.getState() == PullRequestState.CLOSED)
                .filter(pr -> pr.getTiming().getGithubClosedAt() != null)
                .filter(pr -> isWithinDateRange(pr.getTiming().getGithubClosedAt().toLocalDate(), startDate, endDate))
                .collect(Collectors.groupingBy(
                        pr -> getWeekStartDate(pr.getTiming().getGithubClosedAt().toLocalDate())
                ));

        return prsByWeek.entrySet().stream()
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    List<PullRequest> weekPrs = entry.getValue();

                    long mergedCount = weekPrs.stream()
                            .filter(pr -> pr.getState() == PullRequestState.MERGED)
                            .count();

                    long closedCount = weekPrs.stream()
                            .filter(pr -> pr.getState() == PullRequestState.CLOSED)
                            .count();

                    return new WeeklyThroughputDto(weekStart, mergedCount, closedCount);
                })
                .sorted((a, b) -> a.weekStartDate().compareTo(b.weekStartDate()))
                .toList();
    }

    private List<MonthlyThroughputDto> aggregateMonthlyThroughput(
            List<PullRequest> pullRequests,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<YearMonth, List<PullRequest>> prsByMonth = pullRequests.stream()
                .filter(pr -> pr.getState() == PullRequestState.MERGED || pr.getState() == PullRequestState.CLOSED)
                .filter(pr -> pr.getTiming().getGithubClosedAt() != null)
                .filter(pr -> isWithinDateRange(pr.getTiming().getGithubClosedAt().toLocalDate(), startDate, endDate))
                .collect(Collectors.groupingBy(
                        pr -> YearMonth.from(pr.getTiming().getGithubClosedAt().toLocalDate())
                ));

        return prsByMonth.entrySet().stream()
                .map(entry -> {
                    YearMonth yearMonth = entry.getKey();
                    List<PullRequest> monthPrs = entry.getValue();

                    long mergedCount = monthPrs.stream()
                            .filter(pr -> pr.getState() == PullRequestState.MERGED)
                            .count();

                    long closedCount = monthPrs.stream()
                            .filter(pr -> pr.getState() == PullRequestState.CLOSED)
                            .count();

                    return new MonthlyThroughputDto(yearMonth.getYear(), yearMonth.getMonthValue(), mergedCount, closedCount);
                })
                .sorted((a, b) -> compareMonthlyThroughput(a, b))
                .toList();
    }

    private int compareMonthlyThroughput(MonthlyThroughputDto first, MonthlyThroughputDto second) {
        int yearCompare = Integer.compare(first.year(), second.year());
        if (yearCompare != 0) {
            return yearCompare;
        }
        return Integer.compare(first.month(), second.month());
    }

    private List<WeeklyReviewWaitTimeDto> aggregateWeeklyReviewWaitTime(
            List<PullRequest> pullRequests,
            Map<Long, PullRequestBottleneck> bottleneckMap
    ) {
        Map<LocalDate, List<Long>> reviewWaitTimesByWeek = pullRequests.stream()
                .filter(pr -> bottleneckMap.containsKey(pr.getId()))
                .filter(pr -> bottleneckMap.get(pr.getId()).hasReview())
                .filter(pr -> bottleneckMap.get(pr.getId()).getReviewWait() != null)
                .collect(Collectors.groupingBy(
                        pr -> getWeekStartDate(pr.getTiming().getGithubCreatedAt().toLocalDate()),
                        Collectors.mapping(
                                pr -> bottleneckMap.get(pr.getId()).getReviewWait().getMinutes(),
                                Collectors.toList()
                        )
                ));

        return reviewWaitTimesByWeek.entrySet().stream()
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    List<Long> waitTimes = entry.getValue();

                    double avgWaitTime = waitTimes.stream()
                            .mapToLong(value -> value)
                            .average()
                            .orElse(ZERO_DOUBLE);

                    return new WeeklyReviewWaitTimeDto(weekStart, avgWaitTime);
                })
                .sorted((a, b) -> a.weekStartDate().compareTo(b.weekStartDate()))
                .toList();
    }

    private List<WeeklyPrSizeDto> aggregateWeeklyPrSize(
            List<PullRequest> pullRequests,
            Map<Long, PullRequestSize> sizeMap
    ) {
        Map<LocalDate, List<Double>> sizeScoresByWeek = pullRequests.stream()
                .filter(pr -> sizeMap.containsKey(pr.getId()))
                .collect(Collectors.groupingBy(
                        pr -> getWeekStartDate(pr.getTiming().getGithubCreatedAt().toLocalDate()),
                        Collectors.mapping(
                                pr -> sizeMap.get(pr.getId()).getSizeScore().doubleValue(),
                                Collectors.toList()
                        )
                ));

        return sizeScoresByWeek.entrySet().stream()
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    List<Double> sizeScores = entry.getValue();

                    double avgSizeScore = sizeScores.stream()
                            .mapToDouble(value -> value)
                            .average()
                            .orElse(ZERO_DOUBLE);

                    return new WeeklyPrSizeDto(weekStart, avgSizeScore);
                })
                .sorted((a, b) -> a.weekStartDate().compareTo(b.weekStartDate()))
                .toList();
    }

    private LocalDate getWeekStartDate(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private boolean isWithinDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        if (startDate != null && endDate != null) {
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        }
        if (startDate != null) {
            return !date.isBefore(startDate);
        }
        return !date.isAfter(endDate);
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
}
