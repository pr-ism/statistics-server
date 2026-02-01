package com.prism.statistics.presentation.metric;

import com.prism.statistics.application.metric.TrendStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.TrendStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse;
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
public class TrendStatisticsController {

    private final TrendStatisticsQueryService trendStatisticsQueryService;

    @GetMapping("/trends")
    public ResponseEntity<TrendStatisticsResponse> analyzeTrendStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute TrendStatisticsRequest request
    ) {
        TrendStatisticsResponse response = trendStatisticsQueryService.findTrendStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
