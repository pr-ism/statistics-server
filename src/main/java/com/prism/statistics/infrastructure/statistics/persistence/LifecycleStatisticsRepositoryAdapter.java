package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.lifecycle.QPullRequestLifecycle.pullRequestLifecycle;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.statistics.repository.LifecycleStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.LifecycleStatisticsDto;
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
public class LifecycleStatisticsRepositoryAdapter implements LifecycleStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<LifecycleStatisticsDto> findLifecycleStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequestLifecycle> lifecycles = queryFactory
                .selectFrom(pullRequestLifecycle)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestLifecycle.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (lifecycles.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(aggregateStatistics(lifecycles));
    }

    private LifecycleStatisticsDto aggregateStatistics(List<PullRequestLifecycle> lifecycles) {
        long totalCount = lifecycles.size();

        long mergedCount = lifecycles.stream()
                .filter(lc -> lc.getTimeToMerge() != null)
                .count();

        long closedWithoutMergeCount = lifecycles.stream()
                .filter(lc -> lc.getTotalLifespan() != null)
                .filter(lc -> lc.getTimeToMerge() == null)
                .count();

        long closedWithoutReviewCount = lifecycles.stream()
                .filter(lc -> lc.isClosedWithoutReview())
                .count();

        long reopenedCount = lifecycles.stream()
                .filter(lc -> lc.isReopened())
                .count();

        long totalStateChangeCount = lifecycles.stream()
                .mapToInt(lc -> lc.getStateChangeCount())
                .sum();

        long totalTimeToMergeMinutes = lifecycles.stream()
                .filter(lc -> lc.getTimeToMerge() != null)
                .mapToLong(lc -> lc.getTimeToMerge().getMinutes())
                .sum();

        long closedCount = lifecycles.stream()
                .filter(lc -> lc.getTotalLifespan() != null)
                .count();

        long totalLifespanMinutes = lifecycles.stream()
                .filter(lc -> lc.getTotalLifespan() != null)
                .mapToLong(lc -> lc.getTotalLifespan().getMinutes())
                .sum();

        long activeWorkCount = lifecycles.stream()
                .filter(lc -> lc.getActiveWork() != null)
                .count();

        long totalActiveWorkMinutes = lifecycles.stream()
                .filter(lc -> lc.getActiveWork() != null)
                .mapToLong(lc -> lc.getActiveWork().getMinutes())
                .sum();

        return new LifecycleStatisticsDto(
                totalCount,
                mergedCount,
                closedWithoutMergeCount,
                closedWithoutReviewCount,
                reopenedCount,
                totalStateChangeCount,
                mergedCount == 0 ? null : totalTimeToMergeMinutes,
                closedCount == 0 ? null : totalLifespanMinutes,
                activeWorkCount == 0 ? null : totalActiveWorkMinutes
        );
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequestLifecycle.createdAt.goe(startDate.atStartOfDay())
                    .and(pullRequestLifecycle.createdAt.lt(endDate.plusDays(1).atStartOfDay())
            );
        }

        if (startDate != null) {
            return pullRequestLifecycle.createdAt.goe(startDate.atStartOfDay());
        }

        return pullRequestLifecycle.createdAt.lt(endDate.plusDays(1).atStartOfDay());
    }
}
