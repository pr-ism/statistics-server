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

        double mergeSuccessRate = totalClosedPrs > 0
                ? (double) mergedPrCount / totalClosedPrs * 100.0
                : 0.0;

        double avgMergeTimeMinutes = mergedPrCount > 0
                ? (double) dto.totalMergeTimeMinutes() / mergedPrCount
                : 0.0;

        double avgSizeScore = dto.sizeMeasuredCount() > 0 && dto.totalSizeScore() != null
                ? dto.totalSizeScore().doubleValue() / dto.sizeMeasuredCount()
                : 0.0;

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

        double reviewRate = totalPrCount > 0
                ? (double) dto.reviewedPrCount() / totalPrCount * 100.0
                : 0.0;

        double avgReviewWaitMinutes = dto.reviewedPrCount() > 0
                ? (double) dto.totalReviewWaitMinutes() / dto.reviewedPrCount()
                : 0.0;

        double firstReviewApproveRate = dto.reviewedPrCount() > 0
                ? (double) dto.firstReviewApproveCount() / dto.reviewedPrCount() * 100.0
                : 0.0;

        double changesRequestedRate = dto.reviewedPrCount() > 0
                ? (double) dto.changesRequestedCount() / dto.reviewedPrCount() * 100.0
                : 0.0;

        double closedWithoutReviewRate = totalPrCount > 0
                ? (double) dto.closedWithoutReviewCount() / totalPrCount * 100.0
                : 0.0;

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

        double avgReviewersPerPr = uniquePrCount > 0
                ? (double) dto.totalReviewerAssignments() / uniquePrCount
                : 0.0;

        double avgReviewRoundTrips = uniquePrCount > 0
                ? (double) dto.totalReviewRoundTrips() / uniquePrCount
                : 0.0;

        double avgCommentCount = uniquePrCount > 0
                ? (double) dto.totalCommentCount() / uniquePrCount
                : 0.0;

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

        double avgReviewWaitMinutes = count > 0
                ? (double) dto.totalReviewWaitMinutes() / count
                : 0.0;

        double avgReviewProgressMinutes = count > 0
                ? (double) dto.totalReviewProgressMinutes() / count
                : 0.0;

        double avgMergeWaitMinutes = count > 0
                ? (double) dto.totalMergeWaitMinutes() / count
                : 0.0;

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
}
