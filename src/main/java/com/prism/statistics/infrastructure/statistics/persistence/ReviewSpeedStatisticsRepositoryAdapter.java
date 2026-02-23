package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.statistics.repository.ReviewSpeedStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSpeedStatisticsDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewSpeedStatisticsRepositoryAdapter implements ReviewSpeedStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewSpeedStatisticsDto> findReviewSpeedStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime coreTimeStart,
            LocalTime coreTimeEnd
    ) {
        List<PullRequest> pullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (pullRequests.isEmpty()) {
            return Optional.empty();
        }

        List<Long> pullRequestIds = pullRequests.stream()
                .map(pr -> pr.getId())
                .toList();

        List<PullRequestBottleneck> bottlenecks = queryFactory
                .selectFrom(pullRequestBottleneck)
                .where(pullRequestBottleneck.pullRequestId.in(pullRequestIds))
                .fetch();

        Map<Long, PullRequestBottleneck> bottleneckMap = bottlenecks.stream()
                .collect(Collectors.toMap(
                        bottleneck -> bottleneck.getPullRequestId(),
                        b -> b
                ));

        return Optional.of(aggregateStatistics(pullRequests, bottleneckMap, coreTimeStart, coreTimeEnd));
    }

    private ReviewSpeedStatisticsDto aggregateStatistics(
            List<PullRequest> pullRequests,
            Map<Long, PullRequestBottleneck> bottleneckMap,
            LocalTime coreTimeStart,
            LocalTime coreTimeEnd
    ) {
        long totalCount = pullRequests.size();

        List<Long> reviewWaitMinutesList = new ArrayList<>();
        long totalReviewWaitMinutes = 0L;
        long reviewedCount = 0L;
        long totalMergeWaitMinutes = 0L;
        long mergedWithApprovalCount = 0L;
        long coreTimeReviewCount = 0L;
        long sameDayReviewCount = 0L;

        for (PullRequest pr : pullRequests) {
            PullRequestBottleneck bottleneck = bottleneckMap.get(pr.getId());

            if (bottleneck == null || !bottleneck.hasReview()) {
                continue;
            }

            reviewedCount++;

            if (bottleneck.getReviewWait() != null) {
                long reviewWaitMinutes = bottleneck.getReviewWait().getMinutes();
                reviewWaitMinutesList.add(reviewWaitMinutes);
                totalReviewWaitMinutes += reviewWaitMinutes;
            }

            if (bottleneck.getMergeWait() != null && bottleneck.hasApproval()) {
                totalMergeWaitMinutes += bottleneck.getMergeWait().getMinutes();
                mergedWithApprovalCount++;
            }

            if (isWithinCoreTime(bottleneck.getFirstReviewAt().toLocalTime(), coreTimeStart, coreTimeEnd)) {
                coreTimeReviewCount++;
            }

            if (isSameDay(pr.getTiming().getGithubCreatedAt().toLocalDate(),
                    bottleneck.getFirstReviewAt().toLocalDate())) {
                sameDayReviewCount++;
            }
        }

        return new ReviewSpeedStatisticsDto(
                totalCount,
                reviewedCount,
                totalReviewWaitMinutes,
                reviewWaitMinutesList,
                mergedWithApprovalCount,
                totalMergeWaitMinutes,
                coreTimeReviewCount,
                sameDayReviewCount
        );
    }

    private boolean isWithinCoreTime(LocalTime time, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return false;
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }

    private boolean isSameDay(LocalDate date1, LocalDate date2) {
        return date1.equals(date2);
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequest.createdAt.goe(startDate.atStartOfDay())
                    .and(pullRequest.createdAt.lt(endDate.plusDays(1).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequest.createdAt.goe(startDate.atStartOfDay());
        }

        return pullRequest.createdAt.lt(endDate.plusDays(1).atStartOfDay());
    }
}
