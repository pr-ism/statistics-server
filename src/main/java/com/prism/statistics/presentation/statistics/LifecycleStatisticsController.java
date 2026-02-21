package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.LifecycleStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.LifecycleStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse;
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
public class LifecycleStatisticsController {

    private final LifecycleStatisticsQueryService lifecycleStatisticsQueryService;

    @GetMapping("/lifecycle")
    public ResponseEntity<LifecycleStatisticsResponse> getLifecycleStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute LifecycleStatisticsRequest request
    ) {
        LifecycleStatisticsResponse response = lifecycleStatisticsQueryService.findLifecycleStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
