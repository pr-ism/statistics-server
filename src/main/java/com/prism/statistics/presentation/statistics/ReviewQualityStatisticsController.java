package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.ReviewQualityStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ReviewQualityStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse;
import com.prism.statistics.global.auth.AuthUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/statistics")
@RequiredArgsConstructor
public class ReviewQualityStatisticsController {

    private final ReviewQualityStatisticsQueryService reviewQualityStatisticsQueryService;

    @GetMapping("/review-quality")
    public ResponseEntity<ReviewQualityStatisticsResponse> getReviewQualityStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute ReviewQualityStatisticsRequest request
    ) {
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService.findReviewQualityStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
