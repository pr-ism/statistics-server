package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.ReviewCommentEditedService;
import com.prism.statistics.application.webhook.dto.request.ReviewCommentEditedRequest;
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
public class ReviewCommentEditedController {

    private final ReviewCommentEditedService reviewCommentEditedService;

    @PostMapping("/review-comment/edited")
    public ResponseEntity<Void> handleReviewCommentEdited(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentEditedRequest request
    ) {
        reviewCommentEditedService.editReviewComment(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
