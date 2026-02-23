package com.prism.statistics.infrastructure.statistics.persistence;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.statistics.repository.PrSizeStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.PrSizeStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.PrSizeStatisticsDto.PrSizeCorrelationDataDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PrSizeStatisticsRepositoryAdapter implements PrSizeStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<PrSizeStatisticsDto> findPrSizeStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<PullRequestSize> sizes = queryFactory
                .selectFrom(pullRequestSize)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestSize.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetch();

        if (sizes.isEmpty()) {
            return Optional.empty();
        }

        List<Long> pullRequestIds = sizes.stream()
                .map(size -> size.getPullRequestId())
                .toList();

        List<PullRequestBottleneck> bottlenecks = queryFactory
                .selectFrom(pullRequestBottleneck)
                .where(pullRequestBottleneck.pullRequestId.in(pullRequestIds))
                .fetch();

        List<ReviewActivity> activities = queryFactory
                .selectFrom(reviewActivity)
                .where(reviewActivity.pullRequestId.in(pullRequestIds))
                .fetch();

        Map<Long, PullRequestBottleneck> bottleneckMap = bottlenecks.stream()
                .collect(Collectors.toMap(bottleneck -> bottleneck.getPullRequestId(), b -> b));

        Map<Long, ReviewActivity> activityMap = activities.stream()
                .collect(Collectors.toMap(activity -> activity.getPullRequestId(), a -> a));

        return Optional.of(aggregateStatistics(sizes, bottleneckMap, activityMap));
    }

    private PrSizeStatisticsDto aggregateStatistics(
            List<PullRequestSize> sizes,
            Map<Long, PullRequestBottleneck> bottleneckMap,
            Map<Long, ReviewActivity> activityMap
    ) {
        long totalCount = sizes.size();

        BigDecimal totalSizeScore = sizes.stream()
                .map(size -> size.getSizeScore())
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right));

        Map<SizeGrade, Long> sizeGradeDistribution = new EnumMap<>(SizeGrade.class);
        for (SizeGrade grade : SizeGrade.values()) {
            sizeGradeDistribution.put(grade, 0L);
        }
        sizes.stream()
                .collect(Collectors.groupingBy(size -> size.getSizeGrade(), Collectors.counting()))
                .forEach((grade, count) -> sizeGradeDistribution.put(grade, count));

        long largePrCount = sizes.stream()
                .filter(size -> size.isLargeOrAbove())
                .count();

        List<PrSizeCorrelationDataDto> correlationData = new ArrayList<>();
        for (PullRequestSize size : sizes) {
            Long prId = size.getPullRequestId();
            PullRequestBottleneck bottleneck = bottleneckMap.get(prId);
            ReviewActivity activity = activityMap.get(prId);

            Long reviewWaitMinutes = null;
            if (bottleneck != null && bottleneck.getReviewWait() != null) {
                reviewWaitMinutes = bottleneck.getReviewWait().getMinutes();
            }

            Integer reviewRoundTrips = null;
            if (activity != null) {
                reviewRoundTrips = activity.getReviewRoundTrips();
            }

            correlationData.add(new PrSizeCorrelationDataDto(
                    prId,
                    size.getSizeScore(),
                    reviewWaitMinutes,
                    reviewRoundTrips
            ));
        }

        return new PrSizeStatisticsDto(
                totalCount,
                totalSizeScore,
                sizeGradeDistribution,
                largePrCount,
                correlationData
        );
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequestSize.createdAt.goe(startDate.atStartOfDay())
                    .and(pullRequestSize.createdAt.lt(endDate.plusDays(1).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequestSize.createdAt.goe(startDate.atStartOfDay());
        }

        return pullRequestSize.createdAt.lt(endDate.plusDays(1).atStartOfDay());
    }
}
