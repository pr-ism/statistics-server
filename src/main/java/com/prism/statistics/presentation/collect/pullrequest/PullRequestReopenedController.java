package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.collect.ProjectIdResolvingFacade;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReopenedRequest;
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
public class PullRequestReopenedController {

    private final ProjectIdResolvingFacade projectIdResolvingFacade;

    @PostMapping("/reopened")
    public ResponseEntity<Void> handlePullRequestReopened(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestReopenedRequest request
    ) {
        projectIdResolvingFacade.reopenPullRequest(apiKey, request);
        return ResponseEntityConst.NO_CONTENT;
    }
}
