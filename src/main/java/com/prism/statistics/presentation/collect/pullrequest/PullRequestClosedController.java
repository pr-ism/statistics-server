package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestClosedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect/pull-request")
@RequiredArgsConstructor
public class PullRequestClosedController {

    private final PullRequestClosedService pullRequestClosedService;

    @PostMapping("/closed")
    public ResponseEntity<Void> handlePullRequestClosed(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestClosedRequest request
    ) {
        pullRequestClosedService.closePullRequest(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
