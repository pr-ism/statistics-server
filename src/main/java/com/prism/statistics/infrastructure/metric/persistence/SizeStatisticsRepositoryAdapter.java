package com.prism.statistics.infrastructure.metric.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.metric.PullRequestSizeCategory;
import com.prism.statistics.domain.metric.repository.SizeStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.SizeStatisticsDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class SizeStatisticsRepositoryAdapter implements SizeStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<SizeStatisticsDto> findSizeStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Tuple> results = queryFactory
                .select(
                        pullRequest.changeStats.additionCount,
                        pullRequest.changeStats.deletionCount,
                        pullRequest.changeStats.changedFileCount,
                        pullRequest.commitCount
                )
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        goeStartDate(startDate),
                        ltEndDate(endDate)
                )
                .fetch();

        Map<String, List<Tuple>> grouped = results.stream()
                .collect(Collectors.groupingBy(tuple -> {
                    int totalLines = tuple.get(pullRequest.changeStats.additionCount)
                            + tuple.get(pullRequest.changeStats.deletionCount);
                    return PullRequestSizeCategory.classify(totalLines).name();
                }));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<Tuple> tuples = entry.getValue();
                    double avgChangedFileCount = tuples.stream()
                            .mapToInt(t -> t.get(pullRequest.changeStats.changedFileCount))
                            .average()
                            .orElse(0.0);
                    double avgCommitCount = tuples.stream()
                            .mapToInt(t -> t.get(pullRequest.commitCount))
                            .average()
                            .orElse(0.0);
                    return new SizeStatisticsDto(
                            entry.getKey(),
                            tuples.size(),
                            avgChangedFileCount,
                            avgCommitCount
                    );
                })
                .toList();
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
