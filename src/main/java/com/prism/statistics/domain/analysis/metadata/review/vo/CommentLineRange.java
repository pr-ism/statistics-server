package com.prism.statistics.domain.analysis.metadata.review.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLineRange {

    private Integer startLine;
    private int endLine;

    public static CommentLineRange create(Integer startLine, int endLine) {
        if (startLine == null) {
            return single(endLine);
        }
        return range(startLine, endLine);
    }

    private static CommentLineRange single(int endLine) {
        validateLine(endLine);
        return new CommentLineRange(null, endLine);
    }

    private static CommentLineRange range(int startLine, int endLine) {
        validateLine(startLine);
        validateLine(endLine);
        validateRange(startLine, endLine);
        return new CommentLineRange(startLine, endLine);
    }

    private static void validateLine(int line) {
        if (line < 0) {
            throw new IllegalArgumentException("라인은 0보다 작을 수 없습니다.");
        }
    }

    private static void validateRange(int startLine, int endLine) {
        if (startLine >= endLine) {
            throw new IllegalArgumentException("startLine은 endLine보다 작아야 합니다.");
        }
    }

    private CommentLineRange(Integer startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public boolean isSingleLine() {
        return startLine == null;
    }

    public boolean isMultiLine() {
        return startLine != null;
    }
}
