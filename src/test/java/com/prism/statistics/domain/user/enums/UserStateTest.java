package com.prism.statistics.domain.user.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserStateTest {

    @Test
    void 유효한_회원_상태인지_확인한다() {
        // given
        UserState active = UserState.ACTIVE;

        // when
        boolean actual = active.isActive();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void 탈퇴한_회원_상태인지_확인한다() {
        // given
        UserState active = UserState.WITHDRAWAL;

        // when
        boolean actual = active.isWithdrawal();

        // then
        assertThat(actual).isTrue();
    }
}
