package com.prism.statistics.domain.analysis.metadata.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GithubUserTest {

    @Test
    void GithubUser를_생성한다() {
        // when
        GithubUser githubUser = GithubUser.create("octocat", 12345L);

        // then
        assertAll(
                () -> assertThat(githubUser.getUserName()).isEqualTo("octocat"),
                () -> assertThat(githubUser.getUserId()).isEqualTo(12345L)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void 사용자_이름이_비어있으면_예외가_발생한다(String userName) {
        // when & then
        assertThatThrownBy(() -> GithubUser.create(userName, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub 사용자 이름은 필수입니다.");
    }

    @Test
    void 사용자_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> GithubUser.create("octocat", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub 사용자 ID는 필수입니다.");
    }
}
