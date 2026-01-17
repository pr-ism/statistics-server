package com.prism.statistics.domain.user.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NicknameTest {

    @Test
    void 닉네임을_초기화한다() {
        // when
        Nickname actual = Nickname.create("용감한초록");

        // then
        assertThat(actual.getValue()).isEqualTo("용감한초록");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 닉네임이_비어_있으면_초기화할_수_없다(String nickname) {
        // when & then
        assertThatThrownBy(() -> Nickname.create(nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 비어있을 수 없습니다.");
    }

    @Test
    void 최대_길이를_초과하면_닉네임을_초기화할_수_없다() {
        // given
        String invalidNickname = "a".repeat(51);

        // when & then
        assertThatThrownBy(() -> Nickname.create(invalidNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은")
                .hasMessageContaining("글자를 초과할 수 없습니다.");
    }

    @Test
    void 닉네임을_변경한다() {
        // given
        Nickname nickname = Nickname.create("용감한초록");

        // when
        Nickname actual = nickname.changeNickname("변경한 닉네임");

        // then
        assertThat(actual.getValue()).isEqualTo("변경한 닉네임");
    }

    @Test
    void 변경할_닉네임이_길이_제한을_초과하면_닉네임을_변경할_수_없다() {
        // given
        Nickname nickname = Nickname.create("용감한초록");
        String invalidNickname = "a".repeat(51);

        // when & then
        assertThatThrownBy(() -> nickname.changeNickname(invalidNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은")
                .hasMessageContaining("글자를 초과할 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 변경할_닉네임이_비어_있으면_닉네임을_변경할_수_없다(String invalidNickname) {
        // given
        Nickname nickname = Nickname.create("용감한초록");

        // when & then
        assertThatThrownBy(() -> nickname.changeNickname(invalidNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 비어있을 수 없습니다.");
    }
}
