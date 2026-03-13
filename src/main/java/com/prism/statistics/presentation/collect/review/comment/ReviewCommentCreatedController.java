package com.prism.statistics.presentation.collect.review.comment;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentCreatedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.application.collect.inbox.aop.InboxEnqueue;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect/review")
@RequiredArgsConstructor
public class ReviewCommentCreatedController {

    private final ReviewCommentCreatedService reviewCommentCreatedService;

    @InboxEnqueue(CollectInboxType.REVIEW_COMMENT_CREATED)
    @PostMapping("/comment/created")
    public ResponseEntity<Void> handleReviewCommentCreated(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentCreatedRequest request
    ) {
        reviewCommentCreatedService.createReviewComment(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
