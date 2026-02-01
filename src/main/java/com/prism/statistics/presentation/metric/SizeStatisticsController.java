package com.prism.statistics.presentation.metric;

import com.prism.statistics.application.metric.SizeStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.SizeStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse;
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
public class SizeStatisticsController {

    private final SizeStatisticsQueryService sizeStatisticsQueryService;

    @GetMapping("/size")
    public ResponseEntity<SizeStatisticsResponse> analyzeSizeStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute SizeStatisticsRequest request
    ) {
        SizeStatisticsResponse response = sizeStatisticsQueryService.findSizeStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
