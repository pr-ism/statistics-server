package com.prism.statistics.application.pullrequest.dto.response;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;

import java.time.LocalDateTime;
import java.util.Objects;

public record PullRequestDetailResponse(
        Long id,
        int pullRequestNumber,
        String title,
        String state,
        String authorGithubId,
        String link,
        int commitCount,
        PullRequestChangeStatsResponse changeStats,
        PullRequestTimingResponse timing
) {
    public record PullRequestChangeStatsResponse(
            int changedFileCount,
            int additionCount,
            int deletionCount
    ) {
        public static PullRequestChangeStatsResponse from(PullRequestChangeStats changeStats) {
            return new PullRequestChangeStatsResponse(
                    changeStats.getChangedFileCount(),
                    changeStats.getAdditionCount(),
                    changeStats.getDeletionCount()
            );
        }
    }

    public record PullRequestTimingResponse(
            LocalDateTime pullRequestCreatedAt,
            LocalDateTime mergedAt,
            LocalDateTime closedAt
    ) {
        public static PullRequestTimingResponse from(PullRequestTiming pullRequestTiming) {
            return new PullRequestTimingResponse(
                    pullRequestTiming.getPullRequestCreatedAt(),
                    pullRequestTiming.getMergedAt(),
                    pullRequestTiming.getClosedAt()
            );
        }
    }

    public static PullRequestDetailResponse from(PullRequest pullRequest) {
        PullRequestChangeStats pullRequestChangeStats = pullRequest.getChangeStats();

        return new PullRequestDetailResponse(
            pullRequest.getId(),
            pullRequest.getPullRequestNumber(),
            pullRequest.getTitle(),
            pullRequest.getState().name(),
            pullRequest.getAuthor().getUserName(),
            pullRequest.getLink(),
            pullRequest.getCommitCount(),
            PullRequestChangeStatsResponse.from(Objects.requireNonNullElse(pullRequestChangeStats, PullRequestChangeStats.EMPTY)),
            PullRequestTimingResponse.from(pullRequest.getTiming())
        );
    }
}
