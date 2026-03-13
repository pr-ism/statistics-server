package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.collect.ProjectIdResolvingFacade;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.collect.inbox.aop.InboxEnqueue;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
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

    private final ProjectIdResolvingFacade projectIdResolvingFacade;

    @InboxEnqueue(CollectInboxType.PULL_REQUEST_OPENED)
    @PostMapping("/opened")
    public ResponseEntity<Void> handlePullRequestOpened(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestOpenedRequest request
    ) {
        projectIdResolvingFacade.createPullRequest(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
