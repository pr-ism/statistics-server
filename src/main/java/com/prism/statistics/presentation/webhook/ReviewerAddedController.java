package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.ReviewerAddedService;
import com.prism.statistics.application.webhook.dto.request.ReviewerAddedRequest;
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
public class ReviewerAddedController {

    private final ReviewerAddedService reviewerAddedService;

    @PostMapping("/reviewer/added")
    public ResponseEntity<Void> handleReviewerAdded(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewerAddedRequest request
    ) {
        reviewerAddedService.addReviewer(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
