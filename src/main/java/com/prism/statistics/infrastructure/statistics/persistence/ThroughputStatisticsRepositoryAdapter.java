package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.ThroughputStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ThroughputStatisticsDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ThroughputStatisticsRepositoryAdapter implements ThroughputStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<ThroughputStatisticsDto> findThroughputStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequest> pullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (pullRequests.isEmpty()) {
            return Optional.empty();
        }

        long mergedCount = pullRequests.stream()
                .filter(pr -> pr.isMerged())
                .count();

        long closedCount = pullRequests.stream()
                .filter(pr -> pr.isClosed())
                .count();

        long totalMergeTimeMinutes = pullRequests.stream()
                .filter(pr -> pr.isMerged())
                .mapToLong(pr -> pr.calculateMergeTimeMinutes())
                .sum();

        return Optional.of(new ThroughputStatisticsDto(
                mergedCount,
                closedCount,
                totalMergeTimeMinutes
        ));
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
}
