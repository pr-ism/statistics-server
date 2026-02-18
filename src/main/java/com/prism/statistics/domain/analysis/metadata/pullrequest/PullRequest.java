package com.prism.statistics.domain.analysis.metadata.pullrequest;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import java.time.LocalDateTime;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pull_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequest extends CreatedAtEntity {

    private Long githubPullRequestId;

    private Long projectId;

    @Embedded
    private GithubUser author;

    private int pullRequestNumber;

    private String headCommitSha;

    private String title;

    @Enumerated(EnumType.STRING)
    private PullRequestState state;

    private String link;

    @Embedded
    private PullRequestChangeStats changeStats;

    private int commitCount;

    @Embedded
    private PullRequestTiming timing;

    @Builder
    private PullRequest(
            Long githubPullRequestId,
            Long projectId,
            GithubUser author,
            int pullRequestNumber,
            String headCommitSha,
            String title,
            PullRequestState state,
            String link,
            PullRequestChangeStats changeStats,
            int commitCount,
            PullRequestTiming timing
    ) {
        validateFields(githubPullRequestId, projectId, author, pullRequestNumber, headCommitSha, title, state, link, changeStats, commitCount, timing);

        this.githubPullRequestId = githubPullRequestId;
        this.projectId = projectId;
        this.author = author;
        this.pullRequestNumber = pullRequestNumber;
        this.headCommitSha = headCommitSha;
        this.title = title;
        this.state = state;
        this.link = link;
        this.changeStats = changeStats;
        this.commitCount = commitCount;
        this.timing = timing;
    }

    private void validateFields(
            Long githubPullRequestId,
            Long projectId,
            GithubUser author,
            int pullRequestNumber,
            String headCommitSha,
            String title,
            PullRequestState state,
            String link,
            PullRequestChangeStats changeStats,
            int commitCount,
            PullRequestTiming timing
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateProjectId(projectId);
        validateAuthor(author);
        validatePullRequestNumber(pullRequestNumber);
        validateHeadCommitSha(headCommitSha);
        validateTitle(title);
        validateState(state);
        validateLink(link);
        validateChangeStats(changeStats);
        validateCommitCount(commitCount);
        validateTiming(timing);
    }

    private void validateGithubPullRequestId(Long githubPullRequestId) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("GitHub PullRequest ID는 필수입니다.");
        }
    }

    private void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID는 필수입니다.");
        }
    }

    private void validateAuthor(GithubUser author) {
        if (author == null) {
            throw new IllegalArgumentException("작성자 정보는 필수입니다.");
        }
    }

    private void validatePullRequestNumber(int pullRequestNumber) {
        if (pullRequestNumber <= 0) {
            throw new IllegalArgumentException("PullRequest 번호는 양수여야 합니다.");
        }
    }

    private void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("Head Commit SHA는 필수입니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("PullRequest 제목은 필수입니다.");
        }
    }

    private void validateState(PullRequestState state) {
        if (state == null) {
            throw new IllegalArgumentException("PullRequest 상태는 필수입니다.");
        }
    }

    private void validateLink(String link) {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("PullRequest 링크는 필수입니다.");
        }
    }

    private void validateChangeStats(PullRequestChangeStats changeStats) {
        if (changeStats == null) {
            throw new IllegalArgumentException("변경 통계는 필수입니다.");
        }
    }

    private void validateCommitCount(int commitCount) {
        if (commitCount < 0) {
            throw new IllegalArgumentException("커밋 수는 0보다 작을 수 없습니다.");
        }
    }

    private void validateTiming(PullRequestTiming timing) {
        if (timing == null) {
            throw new IllegalArgumentException("시간 정보는 필수입니다.");
        }
    }

    public void synchronize(String headCommitSha, PullRequestChangeStats changeStats, int commitCount) {
        validateHeadCommitSha(headCommitSha);
        validateChangeStats(changeStats);
        validateCommitCount(commitCount);

        this.headCommitSha = headCommitSha;
        this.changeStats = changeStats;
        this.commitCount = commitCount;
    }

    public void changeStateToClosed(LocalDateTime closedAt) {
        this.state = PullRequestState.CLOSED;
        this.timing = PullRequestTiming.createClosed(timing.getGithubCreatedAt(), closedAt);
    }

    public void changeStateToMerged(LocalDateTime mergedAt) {
        this.state = PullRequestState.MERGED;
        this.timing = PullRequestTiming.createMerged(timing.getGithubCreatedAt(), mergedAt);
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

    public long calculateMergeTimeMinutes() {
        if (!isMerged()) {
            throw new IllegalStateException("병합되지 않은 PullRequest 입니다.");
        }
        return timing.calculateMergeTimeMinutes();
    }

}
