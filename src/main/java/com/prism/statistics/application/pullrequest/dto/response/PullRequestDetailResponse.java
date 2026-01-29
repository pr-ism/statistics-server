package com.prism.statistics.application.pullrequest.dto.response;

import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.vo.PrChangeStats;
import com.prism.statistics.domain.pullrequest.vo.PrTiming;
import java.time.LocalDateTime;

public record PullRequestDetailResponse(
        Long id,
        int prNumber,
        String title,
        String state,
        String authorGithubId,
        String link,
        int commitCount,
        ChangeStatsResponse changeStats,
        TimingResponse timing
) {
    public record ChangeStatsResponse(
            int changedFileCount,
            int additionCount,
            int deletionCount
    ) {
        public static ChangeStatsResponse from(PrChangeStats changeStats) {
            return new ChangeStatsResponse(
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
        PrChangeStats changeStats = pullRequest.getChangeStats();
        if (changeStats == null) {
            throw new IllegalStateException("변경 통계가 준비되지 않았습니다.");
        }
        PrTiming timing = pullRequest.getTiming();
        if (timing == null) {
            throw new IllegalStateException("타이밍 정보가 준비되지 않았습니다.");
        }
        return new PullRequestDetailResponse(
                pullRequest.getId(),
                pullRequest.getPrNumber(),
                pullRequest.getTitle(),
                pullRequest.getState().name(),
                pullRequest.getAuthorGithubId(),
                pullRequest.getLink(),
                pullRequest.getCommitCount(),
                ChangeStatsResponse.from(changeStats),
                TimingResponse.from(timing)
        );
    }
}
