package com.prism.statistics.presentation.collect.review.reviewer;

import com.prism.statistics.application.analysis.metadata.review.ReviewerRemovedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
import lombok.RequiredArgsConstructor;
import com.prism.statistics.presentation.common.ResponseEntityConst;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect/review")
@RequiredArgsConstructor
public class ReviewerRemovedController {

    private final ProjectApiKeyService projectApiKeyService;
    private final ReviewerRemovedService reviewerRemovedService;

    @PostMapping("/reviewer/removed")
    public ResponseEntity<Void> handleReviewerRemoved(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewerRemovedRequest request
    ) {
        projectApiKeyService.validateApiKey(apiKey);
        reviewerRemovedService.removeReviewer(request);
        return ResponseEntityConst.NO_CONTENT;
    }
}
