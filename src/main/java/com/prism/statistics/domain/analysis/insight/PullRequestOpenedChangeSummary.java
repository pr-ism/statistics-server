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
@Table(name = "pull_request_opened_change_summaries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestOpenedChangeSummary extends BaseTimeEntity {

    private Long pullRequestId;

    private int totalChanges;

    private BigDecimal avgChangesPerFile;

    public static PullRequestOpenedChangeSummary create(
            Long pullRequestId,
            int totalChanges,
            BigDecimal avgChangesPerFile
    ) {
        validatePullRequestId(pullRequestId);
        validateTotalChanges(totalChanges);
        validateAvgChangesPerFile(avgChangesPerFile);

        return new PullRequestOpenedChangeSummary(pullRequestId, totalChanges, avgChangesPerFile);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private static void validateTotalChanges(int totalChanges) {
        if (totalChanges < 0) {
            throw new IllegalArgumentException("총 변경량은 0보다 작을 수 없습니다.");
        }
    }

    private static void validateAvgChangesPerFile(BigDecimal avgChangesPerFile) {
        if (avgChangesPerFile == null) {
            throw new IllegalArgumentException("파일당 평균 변경량은 필수입니다.");
        }
        if (avgChangesPerFile.signum() < 0) {
            throw new IllegalArgumentException("파일당 평균 변경량은 0보다 작을 수 없습니다.");
        }
    }

    private PullRequestOpenedChangeSummary(Long pullRequestId, int totalChanges, BigDecimal avgChangesPerFile) {
        this.pullRequestId = pullRequestId;
        this.totalChanges = totalChanges;
        this.avgChangesPerFile = avgChangesPerFile;
    }
}
