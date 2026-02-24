package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.PullRequestSizeStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.PullRequestSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PullRequestSizeStatisticsResponse;
import com.prism.statistics.global.auth.AuthUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/statistics")
@RequiredArgsConstructor
public class PullRequestSizeStatisticsController {

    private final PullRequestSizeStatisticsQueryService pullRequestSizeStatisticsQueryService;

    @GetMapping("/pullrequest-size")
    public ResponseEntity<PullRequestSizeStatisticsResponse> getPullRequestSizeStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute PullRequestSizeStatisticsRequest request
    ) {
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService.findPullRequestSizeStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
