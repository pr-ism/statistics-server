package com.prism.statistics.presentation.collect.review;

import com.prism.statistics.application.analysis.metadata.review.ReviewSubmittedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
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
public class ReviewSubmittedController {

    private final ProjectApiKeyService projectApiKeyService;
    private final ReviewSubmittedService reviewSubmittedService;

    @PostMapping("/submitted")
    public ResponseEntity<Void> handleReviewSubmitted(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewSubmittedRequest request
    ) {
        projectApiKeyService.validateApiKey(apiKey);
        reviewSubmittedService.submitReview(request);
        return ResponseEntity.ok().build();
    }
}
