package com.prism.statistics.domain.analysis.metadata.pullrequest.vo;

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
public class PreviousFileName {

    @Column(name = "previous_file_name")
    private String value;

    public static PreviousFileName of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이전 파일명은 필수입니다.");
        }
        return new PreviousFileName(value);
    }

    public static PreviousFileName empty() {
        return new PreviousFileName(null);
    }

    private PreviousFileName(String value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public boolean isPresent() {
        return value != null;
    }
    
}
