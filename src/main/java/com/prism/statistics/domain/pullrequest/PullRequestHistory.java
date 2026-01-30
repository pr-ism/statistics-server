package com.prism.statistics.domain.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.pullrequest.vo.PrChangeStats;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestHistory extends CreatedAtEntity {

    private Long pullRequestId;

    @Embedded
    private PrChangeStats changeStats;

    private int commitCount;

    private LocalDateTime changedAt;

    public static PullRequestHistory create(
            Long pullRequestId,
            PrChangeStats changeStats,
            int commitCount,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateChangeStats(changeStats);
        validateCommitCount(commitCount);
        validateChangedAt(changedAt);
        return new PullRequestHistory(pullRequestId, changeStats, commitCount, changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
        }
    }

    private static void validateChangeStats(PrChangeStats changeStats) {
        if (changeStats == null) {
            throw new IllegalArgumentException("변경 내역은 필수입니다.");
        }
    }

    private static void validateCommitCount(int commitCount) {
        if (commitCount < 0) {
            throw new IllegalArgumentException("커밋 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestHistory(
            Long pullRequestId,
            PrChangeStats changeStats,
            int commitCount,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.changeStats = changeStats;
        this.commitCount = commitCount;
        this.changedAt = changedAt;
    }
}
