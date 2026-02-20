package com.prism.statistics.presentation.collect.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestConvertedToDraftService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestConvertedToDraftRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect/pull-request")
@RequiredArgsConstructor
public class PullRequestConvertedToDraftController {

    private final PullRequestConvertedToDraftService pullRequestConvertedToDraftService;

    @PostMapping("/converted-to-draft")
    public ResponseEntity<Void> handlePullRequestConvertedToDraft(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestConvertedToDraftRequest request
    ) {
        pullRequestConvertedToDraftService.convertToDraft(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
