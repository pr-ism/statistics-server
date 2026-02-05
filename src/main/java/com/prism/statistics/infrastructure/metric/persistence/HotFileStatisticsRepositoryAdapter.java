package com.prism.statistics.infrastructure.metric.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequestFile.pullRequestFile;

import com.prism.statistics.domain.metric.repository.HotFileStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.HotFileStatisticsDto;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class HotFileStatisticsRepositoryAdapter implements HotFileStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HotFileStatisticsDto> findHotFileStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        return queryFactory
                .select(
                        Projections.constructor(
                                HotFileStatisticsDto.class,
                                pullRequestFile.fileName,
                                pullRequestFile.count(),
                                pullRequestFile.fileChanges.additions.sumLong().coalesce(0L),
                                pullRequestFile.fileChanges.deletions.sumLong().coalesce(0L),
                                new CaseBuilder()
                                        .when(pullRequestFile.changeType.eq(FileChangeType.MODIFIED)).then(1L)
                                        .otherwise(0L).sumLong().coalesce(0L),
                                new CaseBuilder()
                                        .when(pullRequestFile.changeType.eq(FileChangeType.ADDED)).then(1L)
                                        .otherwise(0L).sumLong().coalesce(0L),
                                new CaseBuilder()
                                        .when(pullRequestFile.changeType.eq(FileChangeType.REMOVED)).then(1L)
                                        .otherwise(0L).sumLong().coalesce(0L),
                                new CaseBuilder()
                                        .when(pullRequestFile.changeType.eq(FileChangeType.RENAMED)).then(1L)
                                        .otherwise(0L).sumLong().coalesce(0L)
                        )
                )
                .from(pullRequestFile)
                .join(pullRequest).on(pullRequestFile.pullRequestId.eq(pullRequest.id))
                .where(
                        pullRequest.projectId.eq(projectId),
                        goeStartDate(startDate),
                        ltEndDate(endDate)
                )
                .groupBy(pullRequestFile.fileName)
                .orderBy(pullRequestFile.count().desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression goeStartDate(LocalDate startDate) {
        if (startDate == null) {
            return null;
        }

        return pullRequest.timing.pullRequestCreatedAt.goe(startDate.atStartOfDay());
    }

    private BooleanExpression ltEndDate(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }

        return pullRequest.timing.pullRequestCreatedAt.lt(endDate.plusDays(1).atStartOfDay());
    }
}
