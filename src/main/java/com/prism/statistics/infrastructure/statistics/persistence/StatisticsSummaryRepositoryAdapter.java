package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.statistics.repository.StatisticsSummaryRepository;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.BottleneckDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.OverviewDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.ReviewHealthDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.TeamActivityDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.insight.lifecycle.QPullRequestLifecycle.pullRequestLifecycle;
import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;
import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

@Repository
@RequiredArgsConstructor
public class StatisticsSummaryRepositoryAdapter implements StatisticsSummaryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<StatisticsSummaryDto> findStatisticsSummaryByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Long> pullRequestIds = fetchPullRequestIds(projectId, startDate, endDate);

        if (pullRequestIds.isEmpty()) {
            return Optional.empty();
        }

        List<ReviewActivity> activities = fetchReviewActivities(pullRequestIds);
        List<PullRequestBottleneck> bottlenecks = fetchPullRequestBottlenecks(pullRequestIds);

        OverviewDto overview = buildOverviewDto(projectId, pullRequestIds, startDate, endDate);
        ReviewHealthDto reviewHealth = buildReviewHealthDto(pullRequestIds, activities, bottlenecks);
        TeamActivityDto teamActivity = buildTeamActivityDto(pullRequestIds, activities);
        BottleneckDto bottleneck = buildBottleneckDto(bottlenecks);

        return Optional.of(new StatisticsSummaryDto(overview, reviewHealth, teamActivity, bottleneck));
    }

    private List<Long> fetchPullRequestIds(Long projectId, LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .select(pullRequest.id)
                .from(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        createdAtDateRangeCondition(startDate, endDate)
                )
                .fetch();
    }

    private OverviewDto buildOverviewDto(
            Long projectId,
            List<Long> pullRequestIds,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequest> closedPullRequests = queryFactory
                .selectFrom(pullRequest)
                .where(
                        pullRequest.projectId.eq(projectId),
                        pullRequest.id.in(pullRequestIds),
                        pullRequest.state.in(PullRequestState.MERGED, PullRequestState.CLOSED),
                        closedAtDateRangeCondition(startDate, endDate)
                )
                .fetch();

        long totalPrCount = pullRequestIds.size();
        long mergedPrCount = closedPullRequests.stream()
                .filter(pr -> pr.isMerged())
                .count();
        long closedPrCount = closedPullRequests.stream()
                .filter(pr -> pr.isClosed())
                .count();
        long totalMergeTimeMinutes = closedPullRequests.stream()
                .filter(pr -> pr.isMerged())
                .mapToLong(pr -> pr.calculateMergeTimeMinutes())
                .sum();

        List<PullRequestSize> sizes = queryFactory
                .selectFrom(pullRequestSize)
                .where(pullRequestSize.pullRequestId.in(pullRequestIds))
                .fetch();

        BigDecimal totalSizeScore = sizes.stream()
                .map(size -> size.getSizeScore())
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right));

        String dominantSizeGrade = findDominantSizeGrade(sizes);

        return new OverviewDto(
                totalPrCount,
                mergedPrCount,
                closedPrCount,
                totalMergeTimeMinutes,
                totalSizeScore,
                sizes.size(),
                dominantSizeGrade
        );
    }

    private String findDominantSizeGrade(List<PullRequestSize> sizes) {
        if (sizes.isEmpty()) {
            return "N/A";
        }

        Map<SizeGrade, Long> gradeCounts = sizes.stream()
                .collect(Collectors.groupingBy(size -> size.getSizeGrade(), Collectors.counting()));

        return gradeCounts.entrySet().stream()
                .max(Comparator.comparingLong(entry -> entry.getValue()))
                .map(entry -> entry.getKey().name())
                .orElse("N/A");
    }

    private ReviewHealthDto buildReviewHealthDto(
            List<Long> pullRequestIds,
            List<ReviewActivity> activities,
            List<PullRequestBottleneck> bottlenecks
    ) {
        List<PullRequestLifecycle> lifecycles = queryFactory
                .selectFrom(pullRequestLifecycle)
                .where(pullRequestLifecycle.pullRequestId.in(pullRequestIds))
                .fetch();

        long totalPrCount = pullRequestIds.size();
        long reviewedPrCount = activities.stream()
                .filter(a -> a.getTotalCommentCount() > 0 || a.getReviewRoundTrips() > 0)
                .count();

        long totalReviewWaitMinutes = bottlenecks.stream()
                .filter(bottleneck -> bottleneck.hasReview())
                .mapToLong(b -> b.getReviewWait().getMinutes())
                .sum();

        long firstReviewApproveCount = activities.stream()
                .filter(a -> a.getReviewRoundTrips() == 1 && !a.isHasAdditionalReviewers())
                .count();

        long changesRequestedCount = activities.stream()
                .filter(a -> a.getCodeAdditionsAfterReview() > 0 || a.getCodeDeletionsAfterReview() > 0)
                .count();

        long closedWithoutReviewCount = lifecycles.stream()
                .filter(lifecycle -> lifecycle.isClosedWithoutReview())
                .count();

        return new ReviewHealthDto(
                totalPrCount,
                reviewedPrCount,
                totalReviewWaitMinutes,
                firstReviewApproveCount,
                changesRequestedCount,
                closedWithoutReviewCount
        );
    }

    private TeamActivityDto buildTeamActivityDto(List<Long> pullRequestIds, List<ReviewActivity> activities) {
        List<ReviewSession> sessions = queryFactory
                .selectFrom(reviewSession)
                .where(reviewSession.pullRequestId.in(pullRequestIds))
                .fetch();

        long uniqueReviewerCount = sessions.stream()
                .map(s -> s.getReviewer().getUserId())
                .distinct()
                .count();

        long uniquePullRequestCount = sessions.stream()
                .map(session -> session.getPullRequestId())
                .distinct()
                .count();

        long totalReviewerAssignments = sessions.size();

        long totalReviewRoundTrips = activities.stream()
                .mapToLong(activity -> activity.getReviewRoundTrips())
                .sum();

        long totalCommentCount = activities.stream()
                .mapToLong(activity -> activity.getTotalCommentCount())
                .sum();

        double giniCoefficient = calculateGiniCoefficient(sessions);

        return new TeamActivityDto(
                uniqueReviewerCount,
                uniquePullRequestCount,
                totalReviewerAssignments,
                totalReviewRoundTrips,
                totalCommentCount,
                giniCoefficient
        );
    }

    private double calculateGiniCoefficient(List<ReviewSession> sessions) {
        if (sessions.isEmpty()) {
            return 0.0;
        }

        Map<Long, Long> reviewerCounts = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getReviewer().getUserId(),
                        Collectors.counting()
                ));

        List<Long> counts = reviewerCounts.values().stream()
                .sorted()
                .toList();

        int n = counts.size();
        if (n <= 1) {
            return 0.0;
        }

        double totalSum = counts.stream().mapToDouble(value -> value).sum();
        if (totalSum == 0) {
            return 0.0;
        }

        double cumulativeSum = 0;
        double giniSum = 0;
        for (int i = 0; i < n; i++) {
            cumulativeSum += counts.get(i);
            giniSum += (2 * (i + 1) - n - 1) * counts.get(i);
        }

        return giniSum / (n * totalSum);
    }

    private BottleneckDto buildBottleneckDto(List<PullRequestBottleneck> bottlenecks) {
        long totalReviewWaitMinutes = bottlenecks.stream()
                .filter(b -> b.getReviewWait() != null)
                .mapToLong(b -> b.getReviewWait().getMinutes())
                .sum();

        long totalReviewProgressMinutes = bottlenecks.stream()
                .filter(b -> b.getReviewProgress() != null)
                .mapToLong(b -> b.getReviewProgress().getMinutes())
                .sum();

        long totalMergeWaitMinutes = bottlenecks.stream()
                .filter(b -> b.getMergeWait() != null)
                .mapToLong(b -> b.getMergeWait().getMinutes())
                .sum();

        return new BottleneckDto(
                totalReviewWaitMinutes,
                totalReviewProgressMinutes,
                totalMergeWaitMinutes,
                bottlenecks.size()
        );
    }

    private List<ReviewActivity> fetchReviewActivities(List<Long> pullRequestIds) {
        return queryFactory
                .selectFrom(reviewActivity)
                .where(reviewActivity.pullRequestId.in(pullRequestIds))
                .fetch();
    }

    private List<PullRequestBottleneck> fetchPullRequestBottlenecks(List<Long> pullRequestIds) {
        return queryFactory
                .selectFrom(pullRequestBottleneck)
                .where(pullRequestBottleneck.pullRequestId.in(pullRequestIds))
                .fetch();
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
