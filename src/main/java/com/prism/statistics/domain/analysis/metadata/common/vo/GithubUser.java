package com.prism.statistics.domain.analysis.metadata.common.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubUser {

    private String userName;

    private Long userId;

    public static GithubUser create(String userName, Long userId) {
        validateUserName(userName);
        validateUserId(userId);
        return new GithubUser(userName, userId);
    }

    private static void validateUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("GitHub 사용자 이름은 필수입니다.");
        }
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("GitHub 사용자 ID는 필수입니다.");
        }
    }

    private GithubUser(String userName, Long userId) {
        this.userName = userName;
        this.userId = userId;
    }
}
