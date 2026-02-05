package com.prism.statistics.domain.analysis.metadata.pullrequest.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileChanges {

    private int additions;

    private int deletions;

    public static FileChanges create(int additions, int deletions) {
        validateAdditions(additions);
        validateDeletions(deletions);
        return new FileChanges(additions, deletions);
    }

    private static void validateAdditions(int additions) {
        if (additions < 0) {
            throw new IllegalArgumentException("추가 라인 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateDeletions(int deletions) {
        if (deletions < 0) {
            throw new IllegalArgumentException("삭제 라인 수는 0보다 작을 수 없습니다.");
        }
    }

    private FileChanges(int additions, int deletions) {
        this.additions = additions;
        this.deletions = deletions;
    }

    public int getTotalChanges() {
        return additions + deletions;
    }
}
