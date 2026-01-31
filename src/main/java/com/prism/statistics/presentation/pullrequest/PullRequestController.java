package com.prism.statistics.presentation.pullrequest;

import com.prism.statistics.application.pullrequest.PullRequestQueryService;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse;
import com.prism.statistics.global.auth.AuthUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final PullRequestQueryService pullRequestQueryService;

    @GetMapping
    public ResponseEntity<PullRequestListResponse> getPullRequests(
            AuthUserId authUserId,
            @PathVariable Long projectId
    ) {
        PullRequestListResponse response = pullRequestQueryService.findAll(
                authUserId.userId(), projectId
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pullRequestNumber}")
    public ResponseEntity<PullRequestDetailResponse> getPullRequest(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @PathVariable int pullRequestNumber
    ) {
        PullRequestDetailResponse response = pullRequestQueryService.find(
                authUserId.userId(), projectId, pullRequestNumber
        );
        return ResponseEntity.ok(response);
    }
}
