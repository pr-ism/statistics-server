package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.StatisticsSummaryRequest;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.BottleneckSummary;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.OverviewSummary;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.ReviewHealthSummary;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.TeamActivitySummary;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.StatisticsSummaryRepository;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.BottleneckDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.OverviewDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.ReviewHealthDto;
import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto.TeamActivityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsSummaryQueryService {

    private final StatisticsSummaryRepository statisticsSummaryRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public StatisticsSummaryResponse findStatisticsSummary(
            Long userId,
            Long projectId,
            StatisticsSummaryRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return statisticsSummaryRepository
                .findStatisticsSummaryByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(StatisticsSummaryResponse.empty());
    }

    private StatisticsSummaryResponse toResponse(StatisticsSummaryDto dto) {
        return new StatisticsSummaryResponse(
                toOverviewSummary(dto.overview()),
                toReviewHealthSummary(dto.reviewHealth()),
                toTeamActivitySummary(dto.teamActivity()),
                toBottleneckSummary(dto.bottleneck())
        );
    }

    private OverviewSummary toOverviewSummary(OverviewDto dto) {
        long mergedPrCount = dto.mergedPrCount();
        long closedPrCount = dto.closedPrCount();
        long totalClosedPrs = mergedPrCount + closedPrCount;

        double mergeSuccessRate = calculateRate(mergedPrCount, totalClosedPrs);
        double avgMergeTimeMinutes = calculateAverage(dto.totalMergeTimeMinutes(), mergedPrCount);
        double avgSizeScore = calculateAverage(dto.totalSizeScore(), dto.sizeMeasuredCount());

        return OverviewSummary.of(
                dto.totalPrCount(),
                mergedPrCount,
                closedPrCount,
                mergeSuccessRate,
                avgMergeTimeMinutes,
                avgSizeScore,
                dto.dominantSizeGrade()
        );
    }

    private ReviewHealthSummary toReviewHealthSummary(ReviewHealthDto dto) {
        long totalPrCount = dto.totalPrCount();

        double reviewRate = calculateRate(dto.reviewedPrCount(), totalPrCount);
        double avgReviewWaitMinutes = calculateAverage(dto.totalReviewWaitMinutes(), dto.reviewedPrCount());
        double firstReviewApproveRate = calculateRate(dto.firstReviewApproveCount(), dto.reviewedPrCount());
        double changesRequestedRate = calculateRate(dto.changesRequestedCount(), dto.reviewedPrCount());
        double closedWithoutReviewRate = calculateRate(dto.closedWithoutReviewCount(), totalPrCount);

        return ReviewHealthSummary.of(
                reviewRate,
                avgReviewWaitMinutes,
                firstReviewApproveRate,
                changesRequestedRate,
                closedWithoutReviewRate
        );
    }

    private TeamActivitySummary toTeamActivitySummary(TeamActivityDto dto) {
        long uniquePrCount = dto.uniquePullRequestCount();

        double avgReviewersPerPr = calculateAverage(dto.totalReviewerAssignments(), uniquePrCount);
        double avgReviewRoundTrips = calculateAverage(dto.totalReviewRoundTrips(), uniquePrCount);
        double avgCommentCount = calculateAverage(dto.totalCommentCount(), uniquePrCount);

        return TeamActivitySummary.of(
                dto.uniqueReviewerCount(),
                avgReviewersPerPr,
                avgReviewRoundTrips,
                avgCommentCount,
                dto.reviewerGiniCoefficient()
        );
    }

    private BottleneckSummary toBottleneckSummary(BottleneckDto dto) {
        long count = dto.bottleneckCount();

        double avgReviewWaitMinutes = calculateAverage(dto.totalReviewWaitMinutes(), count);
        double avgReviewProgressMinutes = calculateAverage(dto.totalReviewProgressMinutes(), count);
        double avgMergeWaitMinutes = calculateAverage(dto.totalMergeWaitMinutes(), count);

        return BottleneckSummary.of(
                avgReviewWaitMinutes,
                avgReviewProgressMinutes,
                avgMergeWaitMinutes
        );
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0d;
        }
        return numerator * 100.0d / denominator;
    }

    private double calculateAverage(long total, long count) {
        if (count <= 0) {
            return 0.0d;
        }
        return (double) total / count;
    }

    private double calculateAverage(java.math.BigDecimal total, long count) {
        if (count <= 0 || total == null) {
            return 0.0d;
        }
        return total.doubleValue() / count;
    }
}
