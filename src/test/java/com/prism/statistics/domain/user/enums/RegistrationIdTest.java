package com.prism.statistics.domain.user.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RegistrationIdTest {

    @Test
    void 이름으로_등록된_RegistrationId를_찾을_수_있다() {
        // when
        RegistrationId actual = RegistrationId.findBy("kakao");

        // then
        assertThat(actual).isEqualTo(RegistrationId.KAKAO);
    }

    @Test
    void 등록되지_않은_이름이면_RegistrationId를_찾을_수_없다() {
        // when & then
        assertThatThrownBy(() -> RegistrationId.findBy("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 지원하는_RegistrationId_이름인지_여부를_확인한다() {
        // when
        boolean actual = RegistrationId.contains("kakao");

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void 지원하지_않는_RegistrationId_이름인지_여부를_확인한다() {
        // when
        boolean actual = RegistrationId.notContains("facebook");

        // then
        assertThat(actual).isTrue();
    }
}
