package com.prism.statistics.domain.review.vo;

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
public class ReviewBody {

    @Column(name = "body")
    private String value;

    public static ReviewBody create(String value) {
        if (value == null || value.isBlank()) {
            return empty();
        }
        return new ReviewBody(value);
    }

    public static ReviewBody createRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("리뷰 본문은 필수입니다.");
        }
        return new ReviewBody(value);
    }

    private static ReviewBody empty() {
        return new ReviewBody("");
    }

    private ReviewBody(String value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null || value.isBlank();
    }
}
