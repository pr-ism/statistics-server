package com.prism.statistics.domain.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.pullrequest.enums.PrState;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.pullrequest.vo.PrTiming;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pull_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequest extends CreatedAtEntity {

    private Long projectId;

    private String authorGithubId;

    private int prNumber;

    private String title;

    @Enumerated(EnumType.STRING)
    private PrState state;

    private String link;

    @Embedded
    private PullRequestChangeStats changeStats;

    private int commitCount;

    @Embedded
    private PrTiming timing;

    public static PullRequest create(
            Long projectId,
            String authorGithubId,
            int prNumber,
            String title,
            PrState state,
            String link,
            PullRequestChangeStats changeStats,
            int commitCount,
            PrTiming timing
    ) {
        validateProjectId(projectId);
        validateAuthorGithubId(authorGithubId);
        validatePrNumber(prNumber);
        validateTitle(title);
        validateState(state);
        validateLink(link);
        validateChangeStats(changeStats);
        validateCommitCount(commitCount);
        validateTiming(timing);
        return new PullRequest(projectId, authorGithubId, prNumber, title, state, link, changeStats, commitCount, timing);
    }

    public static PullRequest opened(
            Long projectId,
            String authorGithubId,
            int prNumber,
            String title,
            String link,
            PullRequestChangeStats changeStats,
            int commitCount,
            PrTiming timing
    ) {
        return create(projectId, authorGithubId, prNumber, title, PrState.OPEN, link, changeStats, commitCount, timing);
    }

    private static void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID는 필수입니다.");
        }
    }

    private static void validateAuthorGithubId(String authorGithubId) {
        if (authorGithubId == null || authorGithubId.isBlank()) {
            throw new IllegalArgumentException("작성자 GitHub ID는 필수입니다.");
        }
    }

    private static void validatePrNumber(int prNumber) {
        if (prNumber <= 0) {
            throw new IllegalArgumentException("PullRequest 번호는 양수여야 합니다.");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("PullRequest 제목은 필수입니다.");
        }
    }

    private static void validateState(PrState state) {
        if (state == null) {
            throw new IllegalArgumentException("PullRequest 상태는 필수입니다.");
        }
    }

    private static void validateLink(String link) {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("PullRequest 링크는 필수입니다.");
        }
    }

    private static void validateChangeStats(PullRequestChangeStats changeStats) {
        if (changeStats == null) {
            throw new IllegalArgumentException("변경 통계는 필수입니다.");
        }
    }

    private static void validateCommitCount(int commitCount) {
        if (commitCount < 0) {
            throw new IllegalArgumentException("커밋 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateTiming(PrTiming timing) {
        if (timing == null) {
            throw new IllegalArgumentException("시간 정보는 필수입니다.");
        }
    }

    private PullRequest(
            Long projectId,
            String authorGithubId,
            int prNumber,
            String title,
            PrState state,
            String link,
            PullRequestChangeStats changeStats,
            int commitCount,
            PrTiming timing
    ) {
        this.projectId = projectId;
        this.authorGithubId = authorGithubId;
        this.prNumber = prNumber;
        this.title = title;
        this.state = state;
        this.link = link;
        this.changeStats = changeStats;
        this.commitCount = commitCount;
        this.timing = timing;
    }

    public boolean isMerged() {
        return this.state.isMerged();
    }

    public boolean isClosed() {
        return this.state.isClosed();
    }

    public boolean isDraft() {
        return this.state.isDraft();
    }

    public boolean isOpen() {
        return this.state.isOpen();
    }

    public int calculateMergeTimeMinutes() {
        if (!isMerged()) {
            throw new IllegalStateException("병합되지 않은 PR입니다.");
        }
        return timing.calculateMergeTimeMinutes();
    }

    public PrTiming getTimingOrDefault() {
        return this.timing != null ? this.timing : PrTiming.createOpen(this.getCreatedAt());
    }
}
