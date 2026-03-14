package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.collect.CollectFacade;
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

    private final CollectFacade collectFacade;

    @PostMapping("/closed")
    public ResponseEntity<Void> handlePullRequestClosed(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestClosedRequest request
    ) {
        collectFacade.closePullRequest(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
