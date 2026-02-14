package com.prism.statistics.infrastructure.metric.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.metric.repository.TrendStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.TrendStatisticsDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TrendStatisticsRepositoryAdapter implements TrendStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<TrendStatisticsDto> findPullRequestsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return queryFactory
                .select(Projections.constructor(TrendStatisticsDto.class,
                        pullRequest.timing.githubCreatedAt,
                        pullRequest.changeStats.additionCount,
                        pullRequest.changeStats.deletionCount
                ))
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        goeStartDate(startDate),
                        ltEndDate(endDate)
                )
                .fetch();
    }

    private BooleanExpression goeStartDate(LocalDate startDate) {
        if (startDate == null) {
            return null;
        }

        return pullRequest.timing.githubCreatedAt.goe(startDate.atStartOfDay());
    }

    private BooleanExpression ltEndDate(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }

        return pullRequest.timing.githubCreatedAt.lt(endDate.plusDays(1).atStartOfDay());
    }
}
