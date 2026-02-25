package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.DailyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto.DailyPrCountDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<PullRequest> createdPullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        createdAtDateRangeCondition(startDate, endDate)
                )
                .fetch();

        List<DailyPrCountDto> dailyCreatedCounts = aggregateDailyCreatedCounts(createdPullRequests);
        List<DailyPrCountDto> dailyMergedCounts = aggregateDailyMergedCountsFromDb(projectId, startDate, endDate);

        if (dailyCreatedCounts.isEmpty() && dailyMergedCounts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new DailyTrendStatisticsDto(dailyCreatedCounts, dailyMergedCounts));
    }

    private List<DailyPrCountDto> aggregateDailyCreatedCounts(List<PullRequest> pullRequests) {
        Map<LocalDate, Long> countsByDate = pullRequests.stream()
                .collect(Collectors.groupingBy(
                        pr -> pr.getTiming().getGithubCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return countsByDate.entrySet().stream()
                .map(entry -> new DailyPrCountDto(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();
    }

    private List<DailyPrCountDto> aggregateDailyMergedCountsFromDb(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequest> mergedPullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.eq(PullRequestState.MERGED),
                        mergedAtDateRangeCondition(startDate, endDate)
                )
                .fetch();

        Map<LocalDate, Long> countsByDate = mergedPullRequests.stream()
                .filter(pr -> pr.getTiming().getGithubMergedAt() != null)
                .collect(Collectors.groupingBy(
                        pr -> pr.getTiming().getGithubMergedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return countsByDate.entrySet().stream()
                .map(entry -> new DailyPrCountDto(entry.getKey(), entry.getValue()))
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
