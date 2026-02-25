package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.WeeklyTrendStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.WeeklyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse;
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
public class WeeklyTrendStatisticsController {

    private final WeeklyTrendStatisticsQueryService weeklyTrendStatisticsQueryService;

    @GetMapping("/weekly-trend")
    public ResponseEntity<WeeklyTrendStatisticsResponse> getWeeklyTrendStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute WeeklyTrendStatisticsRequest request
    ) {
        WeeklyTrendStatisticsResponse response = weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
