package com.prism.statistics.presentation.metric;

import com.prism.statistics.application.metric.HotFileStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.HotFileStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse;
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
public class HotFileStatisticsController {

    private final HotFileStatisticsQueryService hotFileStatisticsQueryService;

    @GetMapping("/hot-files")
    public ResponseEntity<HotFileStatisticsResponse> analyzeHotFileStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute HotFileStatisticsRequest request
    ) {
        HotFileStatisticsResponse response = hotFileStatisticsQueryService.findHotFileStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
