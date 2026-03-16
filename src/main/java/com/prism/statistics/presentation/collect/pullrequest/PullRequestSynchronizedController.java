package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestSynchronizedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
import lombok.RequiredArgsConstructor;
import com.prism.statistics.presentation.common.ResponseEntityConst;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect/pull-request")
@RequiredArgsConstructor
public class PullRequestSynchronizedController {

    private final ProjectApiKeyService projectApiKeyService;
    private final PullRequestSynchronizedService pullRequestSynchronizedService;

    @PostMapping("/synchronized")
    public ResponseEntity<Void> handlePullRequestSynchronized(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestSynchronizedRequest request
    ) {
        projectApiKeyService.validateApiKey(apiKey);
        pullRequestSynchronizedService.synchronizePullRequest(request);
        return ResponseEntityConst.NO_CONTENT;
    }
}
