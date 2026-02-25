package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.CollaborationStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.AuthorReviewWaitTime;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.DraftPrStatistics;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerAdditionStatistics;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerConcentrationStatistics;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerStats;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.CollaborationStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.AuthorReviewWaitTimeDto;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.ReviewerResponseTimeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborationStatisticsQueryService {

    private static final int TOP_REVIEWER_COUNT = 3;
    private static final double PERCENT_MULTIPLIER = 100.0;
    private static final double GINI_NUMERATOR_MULTIPLIER = 2.0;
    private static final String UNKNOWN_REVIEWER_NAME = "Unknown";

    private final CollaborationStatisticsRepository collaborationStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public CollaborationStatisticsResponse findCollaborationStatistics(
            Long userId,
            Long projectId,
            CollaborationStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return collaborationStatisticsRepository
                .findCollaborationStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(CollaborationStatisticsResponse.empty());
    }

    private CollaborationStatisticsResponse toResponse(CollaborationStatisticsDto dto) {
        long totalCount = dto.totalCount();
        long reviewedCount = dto.reviewedCount();

        ReviewerConcentrationStatistics reviewerConcentration = buildReviewerConcentration(dto.reviewerReviewCounts());
        DraftPrStatistics draftPullRequest = buildDraftPullRequestStatistics(dto, totalCount);
        ReviewerAdditionStatistics reviewerAddition = buildReviewerAdditionStatistics(dto, totalCount);
        List<AuthorReviewWaitTime> authorReviewWaitTimes = buildAuthorReviewWaitTimes(dto.authorReviewWaitTimes());
        List<ReviewerStats> reviewerStats = buildReviewerStats(dto.reviewerReviewCounts(), dto.reviewerResponseTimes());

        return new CollaborationStatisticsResponse(
                totalCount,
                reviewedCount,
                reviewerConcentration,
                draftPullRequest,
                reviewerAddition,
                authorReviewWaitTimes,
                reviewerStats
        );
    }

    private ReviewerConcentrationStatistics buildReviewerConcentration(Map<Long, Long> reviewerReviewCounts) {
        if (reviewerReviewCounts == null || reviewerReviewCounts.isEmpty()) {
            return ReviewerConcentrationStatistics.empty();
        }

        List<Long> counts = new ArrayList<>(reviewerReviewCounts.values());
        double giniCoefficient = calculateGiniCoefficient(counts);
        double top3ReviewerRate = calculateTopReviewerRate(counts);

        return ReviewerConcentrationStatistics.of(giniCoefficient, top3ReviewerRate, reviewerReviewCounts.size());
    }

    private double calculateGiniCoefficient(List<Long> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        int n = values.size();
        if (n == 1) {
            return 0.0;
        }

        List<Long> sorted = values.stream().sorted().toList();
        double sum = sorted.stream().mapToLong(value -> value).sum();

        if (sum == 0) {
            return 0.0;
        }

        double giniSum = 0.0;

        for (int i = 0; i < n; i++) {
            giniSum += (GINI_NUMERATOR_MULTIPLIER * (i + 1) - n - 1) * sorted.get(i);
        }

        return giniSum / (n * sum);
    }

    private double calculateTopReviewerRate(List<Long> counts) {
        if (counts.isEmpty()) {
            return 0.0;
        }

        long totalReviews = counts.stream().mapToLong(value -> value).sum();
        if (totalReviews == 0) {
            return 0.0;
        }

        List<Long> sorted = counts.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        long topSum = sorted.stream()
                .limit(TOP_REVIEWER_COUNT)
                .mapToLong(value -> value)
                .sum();

        return (double) topSum / totalReviews * PERCENT_MULTIPLIER;
    }

    private DraftPrStatistics buildDraftPullRequestStatistics(CollaborationStatisticsDto dto, long totalCount) {
        if (totalCount == 0) {
            return DraftPrStatistics.empty();
        }

        double repeatedDraftPullRequestRate = calculatePercentage(dto.repeatedDraftPrCount(), totalCount);
        return DraftPrStatistics.of(repeatedDraftPullRequestRate, dto.repeatedDraftPrCount());
    }

    private ReviewerAdditionStatistics buildReviewerAdditionStatistics(CollaborationStatisticsDto dto, long totalCount) {
        if (totalCount == 0) {
            return ReviewerAdditionStatistics.empty();
        }

        double reviewerAddedRate = calculatePercentage(dto.reviewerAddedPrCount(), totalCount);
        return ReviewerAdditionStatistics.of(reviewerAddedRate, dto.reviewerAddedPrCount());
    }

    private List<AuthorReviewWaitTime> buildAuthorReviewWaitTimes(List<AuthorReviewWaitTimeDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        return dtos.stream()
                .map(dto -> {
                    return AuthorReviewWaitTime.of(
                            dto.authorId(),
                            dto.authorName(),
                            calculateAverageReviewWaitMinutes(dto),
                            dto.prCount()
                    );
                })
                .sorted(Comparator.comparingDouble((AuthorReviewWaitTime item) -> item.avgReviewWaitMinutes()).reversed())
                .toList();
    }

    private List<ReviewerStats> buildReviewerStats(
            Map<Long, Long> reviewerReviewCounts,
            List<ReviewerResponseTimeDto> responseTimes
    ) {
        if (reviewerReviewCounts == null || reviewerReviewCounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReviewerResponseTimeDto> safeResponseTimes = responseTimes;
        if (safeResponseTimes == null) {
            safeResponseTimes = Collections.emptyList();
        }

        Map<Long, ReviewerResponseTimeDto> responseTimeMap = safeResponseTimes.stream()
                .collect(Collectors.toMap(
                        dto -> dto.reviewerId(),
                        dto -> dto,
                        (a, b) -> a
                ));

        return reviewerReviewCounts.entrySet().stream()
                .map(entry -> {
                    Long reviewerId = entry.getKey();
                    Long reviewCount = entry.getValue();
                    ReviewerResponseTimeDto responseTime = responseTimeMap.get(reviewerId);

                    String reviewerName = resolveReviewerName(responseTime);
                    double avgResponseTimeMinutes = calculateAverageResponseTimeMinutes(responseTime);

                    return ReviewerStats.of(reviewerId, reviewerName, reviewCount, avgResponseTimeMinutes);
                })
                .sorted(Comparator.comparingLong((ReviewerStats item) -> item.reviewCount()).reversed())
                .toList();
    }

    private double calculateAverageReviewWaitMinutes(AuthorReviewWaitTimeDto dto) {
        if (dto.prCount() <= 0) {
            return 0.0;
        }
        return (double) dto.totalReviewWaitMinutes() / dto.prCount();
    }

    private String resolveReviewerName(ReviewerResponseTimeDto responseTime) {
        if (responseTime == null) {
            return UNKNOWN_REVIEWER_NAME;
        }
        return responseTime.reviewerName();
    }

    private double calculateAverageResponseTimeMinutes(ReviewerResponseTimeDto responseTime) {
        if (responseTime == null || responseTime.reviewCount() <= 0) {
            return 0.0;
        }
        return (double) responseTime.totalResponseTimeMinutes() / responseTime.reviewCount();
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculatePercentage(long count, long totalCount) {
        if (totalCount == 0L) {
            return 0.0;
        }
        return Math.round(count * 10000.0 / totalCount) / 100.0;
    }
}
