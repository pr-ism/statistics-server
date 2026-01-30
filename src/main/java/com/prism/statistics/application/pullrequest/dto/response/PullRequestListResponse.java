package com.prism.statistics.application.pullrequest.dto.response;

import com.prism.statistics.domain.pullrequest.PullRequest;
import java.util.List;

public record PullRequestListResponse(
        List<PullRequestSummary> pullRequests
) {
    public record PullRequestSummary(
            Long id,
            int pullRequestNumber,
            String title,
            String state,
            String authorGithubId,
            String link,
            int commitCount
    ) {
        public static PullRequestSummary from(PullRequest pullRequest) {
            return new PullRequestSummary(
                    pullRequest.getId(),
                    pullRequest.getPullRequestNumber(),
                    pullRequest.getTitle(),
                    pullRequest.getState().name(),
                    pullRequest.getAuthorGithubId(),
                    pullRequest.getLink(),
                    pullRequest.getCommitCount()
            );
        }
    }
}
