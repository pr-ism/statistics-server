package com.prism.statistics.infrastructure.metric.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.metric.repository.dto.AuthorStatisticsDto;
import com.prism.statistics.domain.metric.repository.AuthorStatisticsRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AuthorStatisticsRepositoryAdapter implements AuthorStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorStatisticsDto> findAuthorStatisticsByProjectId(Long projectId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                AuthorStatisticsDto.class,
                                pullRequest.author.userName,
                                pullRequest.count(),
                                pullRequest.changeStats.additionCount.sumLong().coalesce(0L),
                                pullRequest.changeStats.deletionCount.sumLong().coalesce(0L),
                                pullRequest.changeStats.additionCount.avg().coalesce(0.0D),
                                pullRequest.changeStats.deletionCount.avg().coalesce(0.0D),
                                pullRequest.commitCount.avg().coalesce(0.0D),
                                pullRequest.changeStats.changedFileCount.avg().coalesce(0.0D)
                        )
                )
                .from(pullRequest)
                .where(pullRequest.projectId.eq(projectId))
                .groupBy(pullRequest.author.userName)
                .fetch();
    }
}
