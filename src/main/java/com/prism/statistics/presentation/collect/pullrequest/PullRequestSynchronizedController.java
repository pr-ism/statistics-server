package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestSynchronizedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.application.collect.inbox.aop.InboxEnqueue;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
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
public class PullRequestSynchronizedController {

    private final PullRequestSynchronizedService pullRequestSynchronizedService;

    @InboxEnqueue(CollectInboxType.PULL_REQUEST_SYNCHRONIZED)
    @PostMapping("/synchronized")
    public ResponseEntity<Void> handlePullRequestSynchronized(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestSynchronizedRequest request
    ) {
        pullRequestSynchronizedService.synchronizePullRequest(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
