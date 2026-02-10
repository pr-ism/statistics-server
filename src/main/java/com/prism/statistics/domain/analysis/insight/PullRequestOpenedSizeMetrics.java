package com.prism.statistics.domain.analysis.insight;

import com.prism.statistics.domain.analysis.insight.enums.PullRequestSizeGrade;
import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pull_request_opened_size_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestOpenedSizeMetrics extends BaseTimeEntity {

    private Long pullRequestId;

    private BigDecimal sizeScore;

    @Enumerated(EnumType.STRING)
    private PullRequestSizeGrade sizeGrade;

    private int changedFileCount;

    private int addedFileCount;

    private int modifiedFileCount;

    private int removedFileCount;

    private int renamedFileCount;

    public static PullRequestOpenedSizeMetrics create(
            Long pullRequestId,
            BigDecimal sizeScore,
            PullRequestSizeGrade sizeGrade,
            int changedFileCount,
            int addedFileCount,
            int modifiedFileCount,
            int removedFileCount,
            int renamedFileCount
    ) {
        validatePullRequestId(pullRequestId);
        validateSizeScore(sizeScore);
        validateSizeGrade(sizeGrade);
        validateFileCount(changedFileCount, "변경 파일 수");
        validateFileCount(addedFileCount, "추가 파일 수");
        validateFileCount(modifiedFileCount, "수정 파일 수");
        validateFileCount(removedFileCount, "삭제 파일 수");
        validateFileCount(renamedFileCount, "이름 변경 파일 수");

        return new PullRequestOpenedSizeMetrics(
                pullRequestId,
                sizeScore,
                sizeGrade,
                changedFileCount,
                addedFileCount,
                modifiedFileCount,
                removedFileCount,
                renamedFileCount
        );
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private static void validateSizeScore(BigDecimal sizeScore) {
        if (sizeScore == null) {
            throw new IllegalArgumentException("크기 점수는 필수입니다.");
        }
        if (sizeScore.signum() < 0) {
            throw new IllegalArgumentException("크기 점수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateSizeGrade(PullRequestSizeGrade sizeGrade) {
        if (sizeGrade == null) {
            throw new IllegalArgumentException("크기 등급은 필수입니다.");
        }
    }

    private static void validateFileCount(int count, String fieldName) {
        if (count < 0) {
            throw new IllegalArgumentException(fieldName + "는 0보다 작을 수 없습니다.");
        }
    }

    private PullRequestOpenedSizeMetrics(
            Long pullRequestId,
            BigDecimal sizeScore,
            PullRequestSizeGrade sizeGrade,
            int changedFileCount,
            int addedFileCount,
            int modifiedFileCount,
            int removedFileCount,
            int renamedFileCount
    ) {
        this.pullRequestId = pullRequestId;
        this.sizeScore = sizeScore;
        this.sizeGrade = sizeGrade;
        this.changedFileCount = changedFileCount;
        this.addedFileCount = addedFileCount;
        this.modifiedFileCount = modifiedFileCount;
        this.removedFileCount = removedFileCount;
        this.renamedFileCount = renamedFileCount;
    }
}
