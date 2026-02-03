package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.ReviewCommentCreatedService;
import com.prism.statistics.application.webhook.dto.request.ReviewCommentCreatedRequest;
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
public class ReviewCommentCreatedController {

    private final ReviewCommentCreatedService reviewCommentCreatedService;

    @PostMapping("/review-comment/created")
    public ResponseEntity<Void> handleReviewCommentCreated(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentCreatedRequest request
    ) {
        reviewCommentCreatedService.createReviewComment(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
