package com.prism.statistics.presentation.collect.review.reviewer;

import com.prism.statistics.application.analysis.metadata.review.ReviewerAddedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerAddedRequest;
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
public class ReviewerAddedController {

    private final ReviewerAddedService reviewerAddedService;

    @InboxEnqueue(CollectInboxType.REVIEWER_ADDED)
    @PostMapping("/reviewer/added")
    public ResponseEntity<Void> handleReviewerAdded(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewerAddedRequest request
    ) {
        reviewerAddedService.addReviewer(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
