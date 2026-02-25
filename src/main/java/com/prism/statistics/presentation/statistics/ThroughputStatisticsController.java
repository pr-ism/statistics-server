package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.ThroughputStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ThroughputStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ThroughputStatisticsResponse;
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
public class ThroughputStatisticsController {

    private final ThroughputStatisticsQueryService throughputStatisticsQueryService;

    @GetMapping("/throughput")
    public ResponseEntity<ThroughputStatisticsResponse> getThroughputStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute ThroughputStatisticsRequest request
    ) {
        ThroughputStatisticsResponse response = throughputStatisticsQueryService.findThroughputStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
