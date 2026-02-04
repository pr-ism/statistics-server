package com.prism.statistics.domain.analysis.insight;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
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
@Table(name = "pull_request_opened_file_change_diversities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestOpenedFileChange extends CreatedAtEntity {

    private Long pullRequestId;

    @Enumerated(EnumType.STRING)
    private FileChangeType changeType;

    private int count;

    private BigDecimal ratio;

    public static PullRequestOpenedFileChange create(
            Long pullRequestId,
            FileChangeType changeType,
            int count,
            BigDecimal ratio
    ) {
        validatePullRequestId(pullRequestId);
        validateChangeType(changeType);
        validateCount(count);
        validateRatio(ratio);

        return new PullRequestOpenedFileChange(pullRequestId, changeType, count, ratio);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private static void validateChangeType(FileChangeType changeType) {
        if (changeType == null) {
            throw new IllegalArgumentException("파일 변경 타입은 필수입니다.");
        }
    }

    private static void validateCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("파일 변경 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateRatio(BigDecimal ratio) {
        if (ratio == null) {
            throw new IllegalArgumentException("파일 변경 비율은 필수입니다.");
        }
        if (ratio.signum() < 0) {
            throw new IllegalArgumentException("파일 변경 비율은 0보다 작을 수 없습니다.");
        }
        if (ratio.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("파일 변경 비율은 1을 초과할 수 없습니다.");
        }
    }

    private PullRequestOpenedFileChange(
            Long pullRequestId,
            FileChangeType changeType,
            int count,
            BigDecimal ratio
    ) {
        this.pullRequestId = pullRequestId;
        this.changeType = changeType;
        this.count = count;
        this.ratio = ratio;
    }
}
