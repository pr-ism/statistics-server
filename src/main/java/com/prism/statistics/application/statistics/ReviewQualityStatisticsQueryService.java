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
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewQualityStatisticsQueryService {

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
        if (dto == null || dto.reviewedCount() == 0) {
            return ReviewActivityStatistics.empty();
        }

        long reviewedCount = dto.reviewedCount();

        double avgReviewRoundTrips = (double) dto.totalReviewRoundTrips() / reviewedCount;
        double avgCommentCount = (double) dto.totalCommentCount() / reviewedCount;
        double avgCommentDensity = dto.totalCommentDensity()
                .divide(BigDecimal.valueOf(reviewedCount), 6, java.math.RoundingMode.HALF_UP)
                .doubleValue();

        return ReviewActivityStatistics.of(
                avgReviewRoundTrips,
                avgCommentCount,
                avgCommentDensity,
                dto.withAdditionalReviewersCount(),
                dto.withChangesAfterReviewCount()
        );
    }

    private ReviewerStatistics buildReviewerStatistics(ReviewSessionStatisticsDto dto) {
        if (dto == null || dto.totalSessionCount() == 0) {
            return ReviewerStatistics.empty();
        }

        long totalSessionCount = dto.totalSessionCount();
        long uniquePullRequestCount = dto.uniquePullRequestCount();

        double avgReviewersPerPr = uniquePullRequestCount > 0
                ? (double) dto.uniqueReviewerCount() / uniquePullRequestCount
                : 0.0;

        double avgSessionDurationMinutes = (double) dto.totalSessionDurationMinutes() / totalSessionCount;
        double avgReviewsPerSession = (double) dto.totalReviewCount() / totalSessionCount;

        return ReviewerStatistics.of(
                dto.uniqueReviewerCount(),
                avgReviewersPerPr,
                avgSessionDurationMinutes,
                avgReviewsPerSession
        );
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculatePercentage(long count, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return Math.round(count * 10000.0 / totalCount) / 100.0;
    }
}
