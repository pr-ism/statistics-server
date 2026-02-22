package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.PrSizeStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.PrSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PrSizeStatisticsResponse;
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
public class PrSizeStatisticsController {

    private final PrSizeStatisticsQueryService prSizeStatisticsQueryService;

    @GetMapping("/pr-size")
    public ResponseEntity<PrSizeStatisticsResponse> getPrSizeStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute PrSizeStatisticsRequest request
    ) {
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService.findPrSizeStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
