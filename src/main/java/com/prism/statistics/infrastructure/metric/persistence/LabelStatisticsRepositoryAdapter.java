package com.prism.statistics.infrastructure.metric.persistence;

import static com.prism.statistics.domain.label.QPullRequestLabel.pullRequestLabel;
import static com.prism.statistics.domain.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.metric.repository.LabelStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.LabelStatisticsDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class LabelStatisticsRepositoryAdapter implements LabelStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<LabelStatisticsDto> findLabelStatisticsByProjectId(Long projectId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                LabelStatisticsDto.class,
                                pullRequestLabel.labelName,
                                pullRequestLabel.count(),
                                pullRequest.changeStats.additionCount.sumLong().coalesce(0L),
                                pullRequest.changeStats.deletionCount.sumLong().coalesce(0L),
                                pullRequest.changeStats.additionCount.avg().coalesce(0.0D),
                                pullRequest.changeStats.deletionCount.avg().coalesce(0.0D),
                                pullRequest.commitCount.avg().coalesce(0.0D),
                                pullRequest.changeStats.changedFileCount.avg().coalesce(0.0D)
                        )
                )
                .from(pullRequestLabel)
                .join(pullRequest).on(pullRequestLabel.pullRequestId.eq(pullRequest.id))
                .where(pullRequest.projectId.eq(projectId))
                .groupBy(pullRequestLabel.labelName)
                .orderBy(pullRequestLabel.count().desc())
                .fetch();
    }
}
