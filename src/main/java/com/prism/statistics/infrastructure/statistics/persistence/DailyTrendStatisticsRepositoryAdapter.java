package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.DailyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto.DailyPrCountDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DailyTrendStatisticsRepositoryAdapter implements DailyTrendStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyTrendStatisticsDto> findDailyTrendStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequest> allPullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        createdAtDateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (allPullRequests.isEmpty()) {
            return Optional.empty();
        }

        List<DailyPrCountDto> dailyCreatedCounts = aggregateDailyCreatedCounts(allPullRequests);
        List<DailyPrCountDto> dailyMergedCounts = aggregateDailyMergedCounts(allPullRequests, startDate, endDate);

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

    private List<DailyPrCountDto> aggregateDailyMergedCounts(
            List<PullRequest> pullRequests,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<LocalDate, Long> countsByDate = pullRequests.stream()
                .filter(pr -> pr.getState() == PullRequestState.MERGED)
                .filter(pr -> pr.getTiming().getGithubMergedAt() != null)
                .filter(pr -> isWithinDateRange(pr.getTiming().getGithubMergedAt().toLocalDate(), startDate, endDate))
                .collect(Collectors.groupingBy(
                        pr -> pr.getTiming().getGithubMergedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return countsByDate.entrySet().stream()
                .map(entry -> new DailyPrCountDto(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();
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
}
