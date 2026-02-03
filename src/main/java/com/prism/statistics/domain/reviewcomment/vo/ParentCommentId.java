package com.prism.statistics.domain.reviewcomment.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentCommentId {

    @Column(name = "parent_comment_id")
    private Long value;

    public static ParentCommentId create(Long value) {
        if (value == null) {
            return empty();
        }
        if (value <= 0) {
            throw new IllegalArgumentException("댓글 ID는 양수여야 합니다.");
        }
        return new ParentCommentId(value);
    }

    private static ParentCommentId empty() {
        return new ParentCommentId(null);
    }

    private ParentCommentId(Long value) {
        this.value = value;
    }

    public boolean hasParent() {
        return value != null;
    }
}
