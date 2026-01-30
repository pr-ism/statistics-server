package com.prism.statistics.domain.reviewer;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.reviewer.enums.ReviewerAction;
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
@Table(name = "requested_reviewer_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestedReviewerHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private String githubMention;

    private Long githubUid;

    @Enumerated(EnumType.STRING)
    private ReviewerAction action;

    private LocalDateTime changedAt;

    public static RequestedReviewerHistory create(
            Long pullRequestId,
            String githubMention,
            Long githubUid,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateGithubMention(githubMention);
        validateGithubUid(githubUid);
        validateAction(action);
        validateChangedAt(changedAt);
        return new RequestedReviewerHistory(pullRequestId, githubMention, githubUid, action, changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
        }
    }

    private static void validateGithubMention(String githubMention) {
        if (githubMention == null || githubMention.isBlank()) {
            throw new IllegalArgumentException("GitHub 멘션은 필수입니다.");
        }
    }

    private static void validateGithubUid(Long githubUid) {
        if (githubUid == null) {
            throw new IllegalArgumentException("GitHub UID는 필수입니다.");
        }
    }

    private static void validateAction(ReviewerAction action) {
        if (action == null) {
            throw new IllegalArgumentException("리뷰어 액션은 필수입니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private RequestedReviewerHistory(
            Long pullRequestId,
            String githubMention,
            Long githubUid,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.githubMention = githubMention;
        this.githubUid = githubUid;
        this.action = action;
        this.changedAt = changedAt;
    }
}
