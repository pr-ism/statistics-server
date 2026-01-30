package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.PullRequestOpenedService;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/pull-request")
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
