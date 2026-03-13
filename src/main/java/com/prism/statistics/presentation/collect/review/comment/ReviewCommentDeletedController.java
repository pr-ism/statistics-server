package com.prism.statistics.presentation.collect.review.comment;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentDeletedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentDeletedRequest;
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
public class ReviewCommentDeletedController {

    private final ReviewCommentDeletedService reviewCommentDeletedService;

    @InboxEnqueue(CollectInboxType.REVIEW_COMMENT_DELETED)
    @PostMapping("/comment/deleted")
    public ResponseEntity<Void> handleReviewCommentDeleted(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentDeletedRequest request
    ) {
        reviewCommentDeletedService.deleteReviewComment(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
