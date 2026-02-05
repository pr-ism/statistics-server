package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestOpenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect/pull-request")
@RequiredArgsConstructor
public class PullRequestOpenedController {

    private final PullRequestOpenedService pullRequestOpenedService;

    @PostMapping("/opened")
    public ResponseEntity<Void> handlePullRequestOpened(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestOpenedRequest request
    ) {
        pullRequestOpenedService.createPullRequest(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
