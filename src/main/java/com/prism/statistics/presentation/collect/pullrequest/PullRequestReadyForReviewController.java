package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReadyForReviewService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
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

    private final PullRequestReadyForReviewService pullRequestReadyForReviewService;

    @PostMapping("/ready-for-review")
    public ResponseEntity<Void> handlePullRequestReadyForReview(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestReadyForReviewRequest request
    ) {
        pullRequestReadyForReviewService.readyForReview(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
