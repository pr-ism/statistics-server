package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.ReviewerRemovedService;
import com.prism.statistics.application.webhook.dto.request.ReviewerRemovedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class ReviewerRemovedController {

    private final ReviewerRemovedService reviewerRemovedService;

    @PostMapping("/reviewer/removed")
    public ResponseEntity<Void> handleReviewerRemoved(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewerRemovedRequest request
    ) {
        reviewerRemovedService.removeReviewer(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
