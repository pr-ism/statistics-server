package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_state_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestStateHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private String headCommitSha;

    @Enumerated(EnumType.STRING)
    private PullRequestState previousState;

    @Enumerated(EnumType.STRING)
    private PullRequestState newState;

    private LocalDateTime changedAt;

    public static PullRequestStateHistory create(
            Long pullRequestId,
            String headCommitSha,
            PullRequestState previousState,
            PullRequestState newState,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateNewState(newState);
        validateChangedAt(changedAt);
        return new PullRequestStateHistory(pullRequestId, headCommitSha, previousState, newState, changedAt);
    }

    public static PullRequestStateHistory createInitial(
            Long pullRequestId,
            String headCommitSha,
            PullRequestState initialState,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateNewState(initialState);
        validateChangedAt(changedAt);
        return new PullRequestStateHistory(pullRequestId, headCommitSha, null, initialState, changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PullRequest ID는 필수입니다.");
        }
    }

    private static void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("Head Commit SHA는 필수입니다.");
        }
    }

    private static void validateNewState(PullRequestState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("새로운 상태는 필수입니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestStateHistory(
            Long pullRequestId,
            String headCommitSha,
            PullRequestState previousState,
            PullRequestState newState,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.headCommitSha = headCommitSha;
        this.previousState = previousState;
        this.newState = newState;
        this.changedAt = changedAt;
    }

    public boolean isInitialState() {
        return previousState == null;
    }
}
