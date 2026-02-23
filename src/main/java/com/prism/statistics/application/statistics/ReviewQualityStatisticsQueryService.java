package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.ReviewQualityStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse.ReviewActivityStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse.ReviewerStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.ReviewQualityStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewActivityStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSessionStatisticsDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewQualityStatisticsQueryService {

    private static final double ZERO_DOUBLE = 0.0d;
    private static final long ZERO_COUNT = 0L;
    private static final int COMMENT_DENSITY_SCALE = 6;
    private static final RoundingMode COMMENT_DENSITY_ROUNDING = RoundingMode.HALF_UP;
    private static final double PERCENT_SCALE = 100.0d;
    private static final double PERCENT_ROUNDING_MULTIPLIER = 10000.0d;

    private final ReviewQualityStatisticsRepository reviewQualityStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ReviewQualityStatisticsResponse findReviewQualityStatistics(
            Long userId,
            Long projectId,
            ReviewQualityStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();

        ReviewActivityStatisticsDto activityDto = reviewQualityStatisticsRepository
                .findReviewActivityStatisticsByProjectId(projectId, startDate, endDate)
                .orElse(null);

        ReviewSessionStatisticsDto sessionDto = reviewQualityStatisticsRepository
                .findReviewSessionStatisticsByProjectId(projectId, startDate, endDate)
                .orElse(null);

        if (activityDto == null && sessionDto == null) {
            return ReviewQualityStatisticsResponse.empty();
        }

        return buildResponse(activityDto, sessionDto);
    }

    private ReviewQualityStatisticsResponse buildResponse(
            ReviewActivityStatisticsDto activityDto,
            ReviewSessionStatisticsDto sessionDto
    ) {
        long totalPullRequestCount = extractTotalCount(activityDto);
        long reviewedPullRequestCount = extractReviewedCount(activityDto);
        double reviewRate = calculatePercentage(reviewedPullRequestCount, totalPullRequestCount);

        ReviewActivityStatistics activityStats = buildActivityStatistics(activityDto);
        ReviewerStatistics reviewerStats = buildReviewerStatistics(sessionDto);

        return new ReviewQualityStatisticsResponse(
                totalPullRequestCount,
                reviewedPullRequestCount,
                reviewRate,
                activityStats,
                reviewerStats
        );
    }

    private long extractTotalCount(ReviewActivityStatisticsDto dto) {
        if (dto == null) {
            return 0L;
        }
        return dto.totalCount();
    }

    private long extractReviewedCount(ReviewActivityStatisticsDto dto) {
        if (dto == null) {
            return 0L;
        }
        return dto.reviewedCount();
    }

    private ReviewActivityStatistics buildActivityStatistics(ReviewActivityStatisticsDto dto) {
        if (isEmptyActivityStatistics(dto)) {
            return ReviewActivityStatistics.empty();
        }

        long reviewedCount = dto.reviewedCount();

        double avgReviewRoundTrips = (double) dto.totalReviewRoundTrips() / reviewedCount;
        double avgCommentCount = (double) dto.totalCommentCount() / reviewedCount;
        double avgCommentDensity = dto.totalCommentDensity()
                .divide(BigDecimal.valueOf(reviewedCount), COMMENT_DENSITY_SCALE, COMMENT_DENSITY_ROUNDING)
                .doubleValue();

        double firstReviewApproveRate = calculatePercentage(dto.firstReviewApproveCount(), reviewedCount);
        double postReviewCommitRate = calculatePercentage(dto.withChangesAfterReviewCount(), reviewedCount);
        double changesRequestedRate = calculatePercentage(dto.changesRequestedCount(), reviewedCount);
        double avgChangesResolutionMinutes = calculateAverage(
                dto.totalChangesResolutionMinutes(), dto.changesResolvedCount());
        double highIntensityPrRate = calculatePercentage(dto.highIntensityPrCount(), reviewedCount);

        return ReviewActivityStatistics.of(
                avgReviewRoundTrips,
                avgCommentCount,
                avgCommentDensity,
                dto.withAdditionalReviewersCount(),
                dto.withChangesAfterReviewCount(),
                firstReviewApproveRate,
                postReviewCommitRate,
                changesRequestedRate,
                avgChangesResolutionMinutes,
                highIntensityPrRate
        );
    }

    private double calculateAverage(long total, long count) {
        if (count == ZERO_COUNT) {
            return ZERO_DOUBLE;
        }
        return (double) total / count;
    }

    private ReviewerStatistics buildReviewerStatistics(ReviewSessionStatisticsDto dto) {
        if (isEmptyReviewerStatistics(dto)) {
            return ReviewerStatistics.empty();
        }

        long totalSessionCount = dto.totalSessionCount();

        double avgReviewersPerPr = calculateAvgReviewersPerPr(dto);
        double avgSessionDurationMinutes = (double) dto.totalSessionDurationMinutes() / totalSessionCount;
        double avgReviewsPerSession = (double) dto.totalReviewCount() / totalSessionCount;

        return ReviewerStatistics.of(
                dto.uniqueReviewerCount(),
                avgReviewersPerPr,
                avgSessionDurationMinutes,
                avgReviewsPerSession
        );
    }

    private double calculateAvgReviewersPerPr(ReviewSessionStatisticsDto dto) {
        long uniquePullRequestCount = dto.uniquePullRequestCount();

        if (uniquePullRequestCount == ZERO_COUNT) {
            return ZERO_DOUBLE;
        }

        return (double) dto.uniqueReviewerCount() / uniquePullRequestCount;
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculatePercentage(long count, long totalCount) {
        if (totalCount == ZERO_COUNT) {
            return ZERO_DOUBLE;
        }
        return Math.round(count * PERCENT_ROUNDING_MULTIPLIER / totalCount) / PERCENT_SCALE;
    }

    private boolean isEmptyActivityStatistics(ReviewActivityStatisticsDto dto) {
        return dto == null || dto.reviewedCount() == ZERO_COUNT;
    }

    private boolean isEmptyReviewerStatistics(ReviewSessionStatisticsDto dto) {
        return dto == null || dto.totalSessionCount() == ZERO_COUNT;
    }
}
