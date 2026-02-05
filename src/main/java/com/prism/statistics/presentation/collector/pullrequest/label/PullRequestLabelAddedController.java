package com.prism.statistics.presentation.collector.pullrequest.label;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelAddedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/pull-request")
@RequiredArgsConstructor
public class PullRequestLabelAddedController {

    private final PullRequestLabelAddedService pullRequestLabelAddedService;

    @PostMapping("/label/added")
    public ResponseEntity<Void> handlePullRequestLabelAdded(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestLabelAddedRequest request
    ) {
        pullRequestLabelAddedService.addPullRequestLabel(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
