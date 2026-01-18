package com.prism.statistics.domain.user.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nickname {

    private static final int MAX_LENGTH = 50;

    private String nicknameValue;

    public static Nickname create(String value) {
        validateValue(value);

        return new Nickname(value);
    }

    private static void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("닉네임은 " + MAX_LENGTH + "글자를 초과할 수 없습니다.");
        }
    }

    private Nickname(String value) {
        this.nicknameValue = value;
    }

    public Nickname changeNickname(String value) {
        return create(value);
    }
}
