package com.prism.statistics.presentation.collect.review.comment;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentDeletedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentDeletedRequest;
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
public class ReviewCommentDeletedController {

    private final ProjectApiKeyService projectApiKeyService;
    private final ReviewCommentDeletedService reviewCommentDeletedService;

    @PostMapping("/comment/deleted")
    public ResponseEntity<Void> handleReviewCommentDeleted(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewCommentDeletedRequest request
    ) {
        projectApiKeyService.validateApiKey(apiKey);
        reviewCommentDeletedService.deleteReviewComment(request);
        return ResponseEntityConst.NO_CONTENT;
    }
}
