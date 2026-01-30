package com.prism.statistics.application.pullrequest.dto.response;

import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.pullrequest.vo.PrTiming;

import java.time.LocalDateTime;
import java.util.Objects;

public record PullRequestDetailResponse(
        Long id,
        int prNumber,
        String title,
        String state,
        String authorGithubId,
        String link,
        int commitCount,
        PullRequestChangeStatsResponse changeStats,
        TimingResponse timing
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

    public record TimingResponse(
            LocalDateTime prCreatedAt,
            LocalDateTime mergedAt,
            LocalDateTime closedAt
    ) {
        public static TimingResponse from(PrTiming timing) {
            return new TimingResponse(
                    timing.getPrCreatedAt(),
                    timing.getMergedAt(),
                    timing.getClosedAt()
            );
        }
    }

    public static PullRequestDetailResponse from(PullRequest pullRequest) {
        PullRequestChangeStats pullRequestChangeStats = pullRequest.getChangeStats();

        return new PullRequestDetailResponse(
            pullRequest.getId(),
            pullRequest.getPrNumber(),
            pullRequest.getTitle(),
            pullRequest.getState().name(),
            pullRequest.getAuthorGithubId(),
            pullRequest.getLink(),
            pullRequest.getCommitCount(),
            PullRequestChangeStatsResponse.from(Objects.requireNonNullElse(pullRequestChangeStats, PullRequestChangeStats.EMPTY)),
            TimingResponse.from(pullRequest.getTimingOrDefault())
        );
    }
}
