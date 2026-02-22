package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.ReviewSpeedStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ReviewSpeedStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse;
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
public class ReviewSpeedStatisticsController {

    private final ReviewSpeedStatisticsQueryService reviewSpeedStatisticsQueryService;

    @GetMapping("/review-speed")
    public ResponseEntity<ReviewSpeedStatisticsResponse> getReviewSpeedStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute ReviewSpeedStatisticsRequest request
    ) {
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
