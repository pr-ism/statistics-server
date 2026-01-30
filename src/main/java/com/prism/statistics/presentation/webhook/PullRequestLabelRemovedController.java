package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.PullRequestLabelRemovedService;
import com.prism.statistics.application.webhook.dto.request.PullRequestLabelRemovedRequest;
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
public class PullRequestLabelRemovedController {

    private final PullRequestLabelRemovedService pullRequestLabelRemovedService;

    @PostMapping("/label/removed")
    public ResponseEntity<Void> handlePullRequestLabelRemoved(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestLabelRemovedRequest request
    ) {
        pullRequestLabelRemovedService.removePullRequestLabel(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
