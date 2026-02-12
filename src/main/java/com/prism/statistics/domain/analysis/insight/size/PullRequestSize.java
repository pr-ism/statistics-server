package com.prism.statistics.domain.analysis.insight.size;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.insight.size.vo.SizeScoreWeight;
import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pull_request_sizes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestSize extends BaseTimeEntity {

    private static final int DIVERSITY_SCALE = 4;

    private Long pullRequestId;

    private BigDecimal sizeScore;

    @Embedded
    private SizeScoreWeight weight;

    @Enumerated(EnumType.STRING)
    private SizeGrade sizeGrade;

    private BigDecimal fileChangeDiversity;

    private int additionCount;

    private int deletionCount;

    private int changedFileCount;

    public static PullRequestSize create(
            Long pullRequestId,
            int additionCount,
            int deletionCount,
            int changedFileCount,
            BigDecimal fileChangeDiversity
    ) {
        return createWithWeight(
                pullRequestId,
                additionCount,
                deletionCount,
                changedFileCount,
                fileChangeDiversity,
                SizeScoreWeight.defaultWeight()
        );
    }

    public static PullRequestSize createWithWeight(
            Long pullRequestId,
            int additionCount,
            int deletionCount,
            int changedFileCount,
            BigDecimal fileChangeDiversity,
            SizeScoreWeight weight
    ) {
        BigDecimal sizeScore = weight.calculateScore(additionCount, deletionCount, changedFileCount);
        SizeGrade sizeGrade = SizeGrade.fromScore(sizeScore);

        return PullRequestSize.builder()
                .pullRequestId(pullRequestId)
                .sizeScore(sizeScore)
                .weight(weight)
                .sizeGrade(sizeGrade)
                .fileChangeDiversity(fileChangeDiversity)
                .additionCount(additionCount)
                .deletionCount(deletionCount)
                .changedFileCount(changedFileCount)
                .build();
    }

    public static BigDecimal calculateFileChangeDiversity(
            int addedCount,
            int modifiedCount,
            int deletedCount,
            int renamedCount
    ) {
        int total = addedCount + modifiedCount + deletedCount + renamedCount;
        if (total == 0) {
            return BigDecimal.ZERO;
        }

        int maxCount = Math.max(Math.max(addedCount, modifiedCount), Math.max(deletedCount, renamedCount));
        BigDecimal maxRatio = BigDecimal.valueOf(maxCount)
                .divide(BigDecimal.valueOf(total), DIVERSITY_SCALE, RoundingMode.HALF_UP);

        return BigDecimal.ONE.subtract(maxRatio).setScale(DIVERSITY_SCALE, RoundingMode.HALF_UP);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private static void validateCounts(int additionCount, int deletionCount, int changedFileCount) {
        if (additionCount < 0) {
            throw new IllegalArgumentException("추가 라인 수는 0보다 작을 수 없습니다.");
        }
        if (deletionCount < 0) {
            throw new IllegalArgumentException("삭제 라인 수는 0보다 작을 수 없습니다.");
        }
        if (changedFileCount < 0) {
            throw new IllegalArgumentException("변경 파일 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateFileChangeDiversity(BigDecimal fileChangeDiversity) {
        if (fileChangeDiversity == null) {
            throw new IllegalArgumentException("파일 변경 다양도는 필수입니다.");
        }
        if (fileChangeDiversity.signum() < 0) {
            throw new IllegalArgumentException("파일 변경 다양도는 0보다 작을 수 없습니다.");
        }
        if (fileChangeDiversity.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("파일 변경 다양도는 1을 초과할 수 없습니다.");
        }
    }

    private static void validateWeight(SizeScoreWeight weight) {
        if (weight == null) {
            throw new IllegalArgumentException("가중치는 필수입니다.");
        }
    }

    @Builder
    private PullRequestSize(
            Long pullRequestId,
            BigDecimal sizeScore,
            SizeScoreWeight weight,
            SizeGrade sizeGrade,
            BigDecimal fileChangeDiversity,
            int additionCount,
            int deletionCount,
            int changedFileCount
    ) {
        validatePullRequestId(pullRequestId);
        validateCounts(additionCount, deletionCount, changedFileCount);
        validateFileChangeDiversity(fileChangeDiversity);
        validateWeight(weight);
        this.pullRequestId = pullRequestId;
        this.sizeScore = sizeScore;
        this.weight = weight;
        this.sizeGrade = sizeGrade;
        this.fileChangeDiversity = fileChangeDiversity;
        this.additionCount = additionCount;
        this.deletionCount = deletionCount;
        this.changedFileCount = changedFileCount;
    }

    public PullRequestSize recalculateWithWeight(SizeScoreWeight newWeight) {
        return createWithWeight(
                this.pullRequestId,
                this.additionCount,
                this.deletionCount,
                this.changedFileCount,
                this.fileChangeDiversity,
                newWeight
        );
    }

    public int getTotalChanges() {
        return additionCount + deletionCount;
    }

    public boolean isLargeOrAbove() {
        return sizeGrade == SizeGrade.L || sizeGrade == SizeGrade.XL;
    }

    public boolean hasHighDiversity() {
        return fileChangeDiversity.compareTo(BigDecimal.valueOf(0.5)) >= 0;
    }
}
