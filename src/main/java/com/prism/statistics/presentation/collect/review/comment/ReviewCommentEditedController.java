package com.prism.statistics.presentation.collect.review.comment;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentEditedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
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
public class ReviewCommentEditedController {

    private final ReviewCommentEditedService reviewCommentEditedService;

    @InboxEnqueue(CollectInboxType.REVIEW_COMMENT_EDITED)
    @PostMapping("/comment/edited")
    public ResponseEntity<Void> handleReviewCommentEdited(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentEditedRequest request
    ) {
        reviewCommentEditedService.editReviewComment(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
