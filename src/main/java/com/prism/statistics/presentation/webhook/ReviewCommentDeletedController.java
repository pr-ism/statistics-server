package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.ReviewCommentDeletedService;
import com.prism.statistics.application.webhook.dto.request.ReviewCommentDeletedRequest;
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
public class ReviewCommentDeletedController {

    private final ReviewCommentDeletedService reviewCommentDeletedService;

    @PostMapping("/review-comment/deleted")
    public ResponseEntity<Void> handleReviewCommentDeleted(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentDeletedRequest request
    ) {
        reviewCommentDeletedService.deleteReviewComment(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
