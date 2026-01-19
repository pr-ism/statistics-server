package com.prism.statistics.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.prism.statistics.domain.user.enums.RegistrationId;
import com.prism.statistics.domain.user.vo.Social;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserIdentityTest {

    @Test
    void 회원의_소셜_정보를_초기화한다() {
        // given
        Social social = new Social(RegistrationId.KAKAO, "social-1");

        // when
        UserIdentity actual = assertDoesNotThrow(() -> UserIdentity.create(1L, social));

        // then
        assertAll(
                () -> assertThat(actual.getUserId()).isEqualTo(1L),
                () -> assertThat(actual.getSocial().getRegistrationId()).isEqualTo(RegistrationId.KAKAO),
                () -> assertThat(actual.getSocial().getSocialId()).isEqualTo("social-1")
        );
    }
}
