package com.prism.statistics.presentation.metric;

import com.prism.statistics.application.metric.ReviewerStatisticsQueryService;
import com.prism.statistics.application.metric.dto.response.ReviewerStatisticsResponse;
import com.prism.statistics.global.auth.AuthUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/statistics")
@RequiredArgsConstructor
public class ReviewerStatisticsController {

    private final ReviewerStatisticsQueryService reviewerStatisticsQueryService;

    @GetMapping("/reviewers")
    public ResponseEntity<ReviewerStatisticsResponse> getReviewerStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId
    ) {
        ReviewerStatisticsResponse response = reviewerStatisticsQueryService.findReviewerStatistics(
                authUserId.userId(),
                projectId
        );

        return ResponseEntity.ok(response);
    }
}
