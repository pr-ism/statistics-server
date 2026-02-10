package com.prism.statistics.infrastructure.metric.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;
import static com.prism.statistics.domain.analysis.metadata.review.QRequestedReviewer.requestedReviewer;

import com.prism.statistics.domain.metric.repository.ReviewerStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.ReviewerStatisticsDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewerStatisticsRepositoryAdapter implements ReviewerStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerStatisticsDto> findReviewerStatisticsByProjectId(Long projectId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                ReviewerStatisticsDto.class,
                                requestedReviewer.reviewer.userName,
                                requestedReviewer.count(),
                                pullRequest.changeStats.additionCount.sumLong().coalesce(0L),
                                pullRequest.changeStats.deletionCount.sumLong().coalesce(0L),
                                pullRequest.changeStats.additionCount.avg().coalesce(0.0D),
                                pullRequest.changeStats.deletionCount.avg().coalesce(0.0D),
                                pullRequest.commitCount.avg().coalesce(0.0D),
                                pullRequest.changeStats.changedFileCount.avg().coalesce(0.0D)
                        )
                )
                .from(requestedReviewer)
                .join(pullRequest).on(requestedReviewer.pullRequestId.eq(pullRequest.id))
                .where(pullRequest.projectId.eq(projectId))
                .groupBy(requestedReviewer.reviewer.userName)
                .fetch();
    }
}
