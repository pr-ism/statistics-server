package com.prism.statistics.infrastructure.statistics.persistence;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.statistics.repository.PullRequestSizeStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.PullRequestSizeStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.PullRequestSizeStatisticsDto.PullRequestSizeCorrelationDataDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.prism.statistics.domain.analysis.insight.activity.QReviewActivity.reviewActivity;
import static com.prism.statistics.domain.analysis.insight.bottleneck.QPullRequestBottleneck.pullRequestBottleneck;
import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

@Repository
@RequiredArgsConstructor
public class PullRequestSizeStatisticsRepositoryAdapter implements PullRequestSizeStatisticsRepository {

    private static final long ZERO_COUNT = 0L;
    private static final long DATE_RANGE_INCLUSIVE_DAYS = 1L;
    private static final List<SizeGrade> LARGE_OR_ABOVE_GRADES = List.of(SizeGrade.L, SizeGrade.XL);

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequestSizeStatisticsDto> findPullRequestSizeStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        NumberExpression<Long> totalCountExpression = pullRequestSize.id.count();
        NumberExpression<Long> xsCountExpression = countSizeGrade(SizeGrade.XS);
        NumberExpression<Long> sCountExpression = countSizeGrade(SizeGrade.S);
        NumberExpression<Long> mCountExpression = countSizeGrade(SizeGrade.M);
        NumberExpression<Long> lCountExpression = countSizeGrade(SizeGrade.L);
        NumberExpression<Long> xlCountExpression = countSizeGrade(SizeGrade.XL);
        NumberExpression<Long> largePullRequestCountExpression = countSizeGrades(LARGE_OR_ABOVE_GRADES);
        NumberExpression<BigDecimal> totalSizeScoreExpression = pullRequestSize.sizeScore.sumBigDecimal();

        Tuple aggregate = queryFactory
                .select(
                        totalCountExpression,
                        totalSizeScoreExpression,
                        xsCountExpression,
                        sCountExpression,
                        mCountExpression,
                        lCountExpression,
                        xlCountExpression,
                        largePullRequestCountExpression
                )
                .from(pullRequestSize)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestSize.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetchOne();

        long totalCount = aggregate == null ? ZERO_COUNT : getLongValue(aggregate.get(totalCountExpression));
        if (totalCount == ZERO_COUNT) {
            return Optional.empty();
        }

        return Optional.of(new PullRequestSizeStatisticsDto(
                totalCount,
                aggregate.get(totalSizeScoreExpression),
                createSizeGradeDistribution(
                        aggregate,
                        xsCountExpression,
                        sCountExpression,
                        mCountExpression,
                        lCountExpression,
                        xlCountExpression
                ),
                getLongValue(aggregate.get(largePullRequestCountExpression)),
                fetchCorrelationData(projectId, startDate, endDate)
        ));
    }

    private List<PullRequestSizeCorrelationDataDto> fetchCorrelationData(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return queryFactory
                .select(Projections.constructor(
                        PullRequestSizeCorrelationDataDto.class,
                        pullRequestSize.sizeScore,
                        pullRequestBottleneck.reviewWait.minutes,
                        reviewActivity.reviewRoundTrips
                ))
                .from(pullRequestSize)
                .join(pullRequest).on(pullRequest.id.eq(pullRequestSize.pullRequestId))
                .leftJoin(pullRequestBottleneck).on(pullRequestBottleneck.pullRequestId.eq(pullRequestSize.pullRequestId))
                .leftJoin(reviewActivity).on(reviewActivity.pullRequestId.eq(pullRequestSize.pullRequestId))
                .where(
                        pullRequest.projectId.eq(projectId),
                        dateRangeCondition(startDate, endDate)
                )
                .fetch();
    }

    private Map<SizeGrade, Long> createSizeGradeDistribution(
            Tuple aggregate,
            NumberExpression<Long> xsCountExpression,
            NumberExpression<Long> sCountExpression,
            NumberExpression<Long> mCountExpression,
            NumberExpression<Long> lCountExpression,
            NumberExpression<Long> xlCountExpression
    ) {
        Map<SizeGrade, Long> distribution = new EnumMap<>(SizeGrade.class);
        distribution.put(SizeGrade.XS, getLongValue(aggregate.get(xsCountExpression)));
        distribution.put(SizeGrade.S, getLongValue(aggregate.get(sCountExpression)));
        distribution.put(SizeGrade.M, getLongValue(aggregate.get(mCountExpression)));
        distribution.put(SizeGrade.L, getLongValue(aggregate.get(lCountExpression)));
        distribution.put(SizeGrade.XL, getLongValue(aggregate.get(xlCountExpression)));
        return distribution;
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return pullRequestSize.createdAt.goe(startDate.atStartOfDay())
                    .and(pullRequestSize.createdAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay()));
        }

        if (startDate != null) {
            return pullRequestSize.createdAt.goe(startDate.atStartOfDay());
        }

        return pullRequestSize.createdAt.lt(endDate.plusDays(DATE_RANGE_INCLUSIVE_DAYS).atStartOfDay());
    }

    private NumberExpression<Long> countSizeGrade(SizeGrade sizeGrade) {
        return new CaseBuilder()
                .when(pullRequestSize.sizeGrade.eq(sizeGrade))
                .then(1)
                .otherwise(0)
                .sumLong();
    }

    private NumberExpression<Long> countSizeGrades(List<SizeGrade> sizeGrades) {
        return new CaseBuilder()
                .when(pullRequestSize.sizeGrade.in(sizeGrades))
                .then(1)
                .otherwise(0)
                .sumLong();
    }

    private long getLongValue(Number number) {
        if (number == null) {
            return ZERO_COUNT;
        }
        return number.longValue();
    }
}
