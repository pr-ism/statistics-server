package com.prism.statistics.domain.analysis.insight;

import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pull_request_opened_commit_densities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestOpenedCommitDensity extends BaseTimeEntity {

    private Long pullRequestId;

    private BigDecimal commitDensityPerFile;

    private BigDecimal commitDensityPerChange;

    public static PullRequestOpenedCommitDensity create(
            Long pullRequestId,
            BigDecimal commitDensityPerFile,
            BigDecimal commitDensityPerChange
    ) {
        validatePullRequestId(pullRequestId);
        validateCommitDensityPerFile(commitDensityPerFile);
        validateCommitDensityPerChange(commitDensityPerChange);

        return new PullRequestOpenedCommitDensity(pullRequestId, commitDensityPerFile, commitDensityPerChange);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
        }
    }

    private static void validateCommitDensityPerFile(BigDecimal commitDensityPerFile) {
        if (commitDensityPerFile == null) {
            throw new IllegalArgumentException("파일 기준 커밋 밀도는 필수입니다.");
        }
        if (commitDensityPerFile.signum() < 0) {
            throw new IllegalArgumentException("파일 기준 커밋 밀도는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateCommitDensityPerChange(BigDecimal commitDensityPerChange) {
        if (commitDensityPerChange == null) {
            throw new IllegalArgumentException("변경량 기준 커밋 밀도는 필수입니다.");
        }
        if (commitDensityPerChange.signum() < 0) {
            throw new IllegalArgumentException("변경량 기준 커밋 밀도는 0보다 작을 수 없습니다.");
        }
    }

    private PullRequestOpenedCommitDensity(
            Long pullRequestId,
            BigDecimal commitDensityPerFile,
            BigDecimal commitDensityPerChange
    ) {
        this.pullRequestId = pullRequestId;
        this.commitDensityPerFile = commitDensityPerFile;
        this.commitDensityPerChange = commitDensityPerChange;
    }
}
