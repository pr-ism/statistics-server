package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.collect.ProjectIdResolvingFacade;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
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
public class PullRequestReadyForReviewController {

    private final ProjectIdResolvingFacade projectIdResolvingFacade;

    @InboxEnqueue(CollectInboxType.PULL_REQUEST_READY_FOR_REVIEW)
    @PostMapping("/ready-for-review")
    public ResponseEntity<Void> handlePullRequestReadyForReview(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestReadyForReviewRequest request
    ) {
        projectIdResolvingFacade.readyForReview(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
