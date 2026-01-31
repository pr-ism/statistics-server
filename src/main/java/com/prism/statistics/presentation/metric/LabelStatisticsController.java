package com.prism.statistics.presentation.metric;

import com.prism.statistics.application.metric.LabelStatisticsQueryService;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse;
import com.prism.statistics.global.auth.AuthUserId;
import com.prism.statistics.application.metric.dto.request.LabelStatisticsRequest;
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
public class LabelStatisticsController {

    private final LabelStatisticsQueryService labelStatisticsQueryService;

    @GetMapping("/labels")
    public ResponseEntity<LabelStatisticsResponse> analyzeLabelStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute LabelStatisticsRequest request
    ) {
        LabelStatisticsResponse response = labelStatisticsQueryService.findLabelStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
