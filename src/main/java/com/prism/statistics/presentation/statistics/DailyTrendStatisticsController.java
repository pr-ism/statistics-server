package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.DailyTrendStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.DailyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse;
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
public class DailyTrendStatisticsController {

    private final DailyTrendStatisticsQueryService dailyTrendStatisticsQueryService;

    @GetMapping("/daily-trend")
    public ResponseEntity<DailyTrendStatisticsResponse> getDailyTrendStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute DailyTrendStatisticsRequest request
    ) {
        DailyTrendStatisticsResponse response = dailyTrendStatisticsQueryService.findDailyTrendStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
