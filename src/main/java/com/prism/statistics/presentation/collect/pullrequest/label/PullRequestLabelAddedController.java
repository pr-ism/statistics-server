package com.prism.statistics.presentation.collect.pullrequest.label;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelAddedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
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
@RequestMapping("/collect/pull-request")
@RequiredArgsConstructor
public class PullRequestLabelAddedController {

    private final ProjectApiKeyService projectApiKeyService;
    private final PullRequestLabelAddedService pullRequestLabelAddedService;

    @PostMapping("/label/added")
    public ResponseEntity<Void> handlePullRequestLabelAdded(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PullRequestLabelAddedRequest request
    ) {
        projectApiKeyService.validateApiKey(apiKey);
        pullRequestLabelAddedService.addPullRequestLabel(request);
        return ResponseEntityConst.NO_CONTENT;
    }
}
