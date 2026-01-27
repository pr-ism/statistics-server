package com.prism.statistics.domain.pullrequest.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrChangeStats {

    private int changedFileCount;

    private int additionCount;

    private int deletionCount;

    public static PrChangeStats create(int changedFileCount, int additionCount, int deletionCount) {
        validateChangedFileCount(changedFileCount);
        validateAdditionCount(additionCount);
        validateDeletionCount(deletionCount);
        validateChangesConsistency(changedFileCount, additionCount, deletionCount);
        return new PrChangeStats(changedFileCount, additionCount, deletionCount);
    }

    private static void validateChangedFileCount(int changedFileCount) {
        if (changedFileCount < 0) {
            throw new IllegalArgumentException("변경 파일 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateAdditionCount(int additionCount) {
        if (additionCount < 0) {
            throw new IllegalArgumentException("추가 라인 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateDeletionCount(int deletionCount) {
        if (deletionCount < 0) {
            throw new IllegalArgumentException("삭제 라인 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateChangesConsistency(int changedFileCount, int additionCount, int deletionCount) {
        if (changedFileCount == 0 && (additionCount > 0 || deletionCount > 0)) {
            throw new IllegalArgumentException("변경된 파일이 없으면 추가/삭제 라인이 있을 수 없습니다.");
        }
    }

    private PrChangeStats(int changedFileCount, int additionCount, int deletionCount) {
        this.changedFileCount = changedFileCount;
        this.additionCount = additionCount;
        this.deletionCount = deletionCount;
    }

    public int getTotalChanges() {
        return additionCount + deletionCount;
    }
}
