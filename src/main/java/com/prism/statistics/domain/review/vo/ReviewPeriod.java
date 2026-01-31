package com.prism.statistics.domain.review.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewPeriod {

    private LocalDateTime firstCommentedAt;

    private LocalDateTime lastCommentedAt;

    public static ReviewPeriod empty() {
        return new ReviewPeriod(null, null);
    }

    public static ReviewPeriod create(LocalDateTime firstCommentedAt, LocalDateTime lastCommentedAt) {
        if (firstCommentedAt != null && lastCommentedAt != null) {
            validateOrder(firstCommentedAt, lastCommentedAt);
        }
        return new ReviewPeriod(firstCommentedAt, lastCommentedAt);
    }

    private static void validateOrder(LocalDateTime first, LocalDateTime last) {
        if (first.isAfter(last)) {
            throw new IllegalArgumentException("첫 번째 코멘트 시각은 마지막 코멘트 시각보다 이전이어야 합니다.");
        }
    }

    private ReviewPeriod(LocalDateTime firstCommentedAt, LocalDateTime lastCommentedAt) {
        this.firstCommentedAt = firstCommentedAt;
        this.lastCommentedAt = lastCommentedAt;
    }
}
