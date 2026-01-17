package com.prism.statistics.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.domain.user.vo.Nickname;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NicknameGeneratorTest {

    @Test
    void 닉네임을_생성한다() {
        // given
        NicknameGenerator nicknameGenerator = NicknameGenerator.of(
                List.of("섬세한", "용감한"),
                List.of("보라", "초록")
        );

        // when
        Nickname actual = nicknameGenerator.generate(bound -> 1);

        // then
        assertThat(actual.getNicknameValue()).isEqualTo("용감한초록");
    }

    @Test
    void 형용사가_비어있으면_닉네임을_생성기를_초기화할_수_없다() {
        // when & then
        assertThatThrownBy(() -> NicknameGenerator.of(List.of(), List.of("보라")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임 형용사 목록이 비어있습니다.");
    }

    @Test
    void 색상이_비어있으면_닉네임_생성기를_초기화할_수_없다() {
        // when & then
        assertThatThrownBy(() -> NicknameGenerator.of(List.of("섬세한"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임 색상 목록이 비어있습니다.");
    }

    @Test
    void 닉네임_생성_시_인덱스_생성기가_비어_있으면_닉네임을_생성할_수_없다() {
        // given
        NicknameGenerator nicknameGenerator = NicknameGenerator.of(
                List.of("섬세한", "용감한"),
                List.of("보라", "초록")
        );

        // when & then
        assertThatThrownBy(() -> nicknameGenerator.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임 인덱스 생성 컴포넌트가 필요합니다.");
    }
}
