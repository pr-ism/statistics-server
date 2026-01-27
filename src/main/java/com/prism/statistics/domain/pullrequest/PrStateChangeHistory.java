package com.prism.statistics.domain.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.pullrequest.enums.PrState;
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
@Table(name = "pr_state_change_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrStateChangeHistory extends CreatedAtEntity {

    private Long pullRequestId;

    @Enumerated(EnumType.STRING)
    private PrState previousState;

    @Enumerated(EnumType.STRING)
    private PrState newState;

    private LocalDateTime changedAt;

    public static PrStateChangeHistory create(
            Long pullRequestId,
            PrState previousState,
            PrState newState,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateNewState(newState);
        validateChangedAt(changedAt);
        return new PrStateChangeHistory(pullRequestId, previousState, newState, changedAt);
    }

    public static PrStateChangeHistory createInitial(Long pullRequestId, PrState initialState, LocalDateTime changedAt) {
        validatePullRequestId(pullRequestId);
        validateNewState(initialState);
        validateChangedAt(changedAt);
        return new PrStateChangeHistory(pullRequestId, null, initialState, changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
        }
    }

    private static void validateNewState(PrState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("새로운 상태는 필수입니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PrStateChangeHistory(
            Long pullRequestId,
            PrState previousState,
            PrState newState,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.previousState = previousState;
        this.newState = newState;
        this.changedAt = changedAt;
    }

    public boolean isInitialState() {
        return previousState == null;
    }
}
