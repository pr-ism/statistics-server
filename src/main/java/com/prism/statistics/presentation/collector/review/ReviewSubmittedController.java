package com.prism.statistics.presentation.collector.review;

import com.prism.statistics.application.analysis.metadata.review.ReviewSubmittedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
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
public class ReviewSubmittedController {

    private final ReviewSubmittedService reviewSubmittedService;

    @PostMapping("/review/submitted")
    public ResponseEntity<Void> handleReviewSubmitted(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody ReviewSubmittedRequest request
    ) {
        reviewSubmittedService.submitReview(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
