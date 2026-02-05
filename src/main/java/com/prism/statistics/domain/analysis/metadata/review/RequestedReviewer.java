
package com.prism.statistics.domain.analysis.metadata.review;

import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "requested_reviewers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestedReviewer extends CreatedAtEntity {

    private Long pullRequestId;

    private String githubMention;

    private Long githubUid;

    private LocalDateTime requestedAt;

    public static RequestedReviewer create(
            Long pullRequestId,
            String githubMention,
            Long githubUid,
            LocalDateTime requestedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateGithubMention(githubMention);
        validateGithubUid(githubUid);
        validateRequestedAt(requestedAt);
        return new RequestedReviewer(pullRequestId, githubMention, githubUid, requestedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PullRequest ID는 필수입니다.");
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

    private static void validateRequestedAt(LocalDateTime requestedAt) {
        if (requestedAt == null) {
            throw new IllegalArgumentException("리뷰어 요청 시각은 필수입니다.");
        }
    }

    private RequestedReviewer(
            Long pullRequestId,
            String githubMention,
            Long githubUid,
            LocalDateTime requestedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.githubMention = githubMention;
        this.githubUid = githubUid;
        this.requestedAt = requestedAt;
    }
}
