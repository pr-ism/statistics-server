package com.prism.statistics.domain.user.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SocialTest {

    @Test
    void 소셜_정보를_초기화한다() {
        // when & then
        Social actual = assertDoesNotThrow(() ->new Social(RegistrationId.KAKAO, "social-1"));

        assertAll(
                () -> assertThat(actual.getRegistrationId()).isEqualTo(RegistrationId.KAKAO),
                () -> assertThat(actual.getSocialId()).isEqualTo("social-1")
        );
    }
}
