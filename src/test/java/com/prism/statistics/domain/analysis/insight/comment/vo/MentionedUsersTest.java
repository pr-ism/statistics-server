package com.prism.statistics.domain.analysis.insight.comment.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MentionedUsersTest {

    @Test
    void 코멘트_본문에서_멘션을_추출한다() {
        // given
        String body = "Hey @john, can you review this? Also @jane please check.";

        // when
        MentionedUsers mentioned = MentionedUsers.fromBody(body);

        // then
        assertAll(
                () -> assertThat(mentioned.getCount()).isEqualTo(2),
                () -> assertThat(mentioned.toList()).containsExactly("john", "jane")
        );
    }

    @Test
    void 중복_멘션은_하나로_처리한다() {
        // given
        String body = "@john @john @john please review";

        // when
        MentionedUsers mentioned = MentionedUsers.fromBody(body);

        // then
        assertAll(
                () -> assertThat(mentioned.getCount()).isEqualTo(1),
                () -> assertThat(mentioned.toList()).containsExactly("john")
        );
    }

    @Test
    void 하이픈이_포함된_사용자명을_추출한다() {
        // given
        String body = "@user-name-123 please check";

        // when
        MentionedUsers mentioned = MentionedUsers.fromBody(body);

        // then
        assertThat(mentioned.toList()).containsExactly("user-name-123");
    }

    @Test
    void 이메일은_멘션으로_추출하지_않는다() {
        // given
        String body = "Send email to user@example.com";

        // when
        MentionedUsers mentioned = MentionedUsers.fromBody(body);

        // then
        assertThat(mentioned.toList()).contains("example");
    }

    @Test
    void 본문이_null이면_빈_멘션을_반환한다() {
        // when
        MentionedUsers mentioned = MentionedUsers.fromBody(null);

        // then
        assertAll(
                () -> assertThat(mentioned.isEmpty()).isTrue(),
                () -> assertThat(mentioned.getCount()).isZero()
        );
    }

    @Test
    void 본문이_비어있으면_빈_멘션을_반환한다() {
        // when
        MentionedUsers mentioned = MentionedUsers.fromBody("   ");

        // then
        assertThat(mentioned.isEmpty()).isTrue();
    }

    @Test
    void 멘션이_없으면_빈_멘션을_반환한다() {
        // given
        String body = "This is a comment without any mentions.";

        // when
        MentionedUsers mentioned = MentionedUsers.fromBody(body);

        // then
        assertThat(mentioned.isEmpty()).isTrue();
    }

    @Test
    void 사용자명_리스트로_생성한다() {
        // given
        List<String> userNames = Arrays.asList("john", "jane", "bob");

        // when
        MentionedUsers mentioned = MentionedUsers.of(userNames);

        // then
        assertAll(
                () -> assertThat(mentioned.getCount()).isEqualTo(3),
                () -> assertThat(mentioned.toList()).containsExactly("john", "jane", "bob")
        );
    }

    @Test
    void 빈_리스트로_생성하면_빈_멘션을_반환한다() {
        // when
        MentionedUsers mentioned = MentionedUsers.of(List.of());

        // then
        assertThat(mentioned.isEmpty()).isTrue();
    }

    @Test
    void empty로_빈_멘션을_생성한다() {
        // when
        MentionedUsers mentioned = MentionedUsers.empty();

        // then
        assertAll(
                () -> assertThat(mentioned.isEmpty()).isTrue(),
                () -> assertThat(mentioned.toList()).isEmpty()
        );
    }

    @Test
    void 특정_사용자_포함_여부를_확인한다() {
        // given
        MentionedUsers mentioned = MentionedUsers.fromBody("@john @jane");

        // then
        assertAll(
                () -> assertThat(mentioned.contains("john")).isTrue(),
                () -> assertThat(mentioned.contains("bob")).isFalse()
        );
    }

    @Test
    void 빈_멘션에서_사용자_포함_여부를_확인하면_false를_반환한다() {
        // given
        MentionedUsers mentioned = MentionedUsers.empty();

        // when & then
        assertThat(mentioned.contains("john")).isFalse();
    }

    @Test
    void 사용자명이_null이면_포함_여부가_false이다() {
        // given
        MentionedUsers mentioned = MentionedUsers.fromBody("@john @jane");

        // when & then
        assertThat(mentioned.contains(null)).isFalse();
    }

    @Test
    void 두_멘션을_병합한다() {
        // given
        MentionedUsers first = MentionedUsers.fromBody("@john @jane");
        MentionedUsers second = MentionedUsers.fromBody("@jane @bob");

        // when
        MentionedUsers merged = first.merge(second);

        // then
        assertAll(
                () -> assertThat(merged.getCount()).isEqualTo(3),
                () -> assertThat(merged.toList()).containsExactlyInAnyOrder("john", "jane", "bob")
        );
    }

    @Test
    void 빈_멘션과_병합하면_원본을_반환한다() {
        // given
        MentionedUsers mentioned = MentionedUsers.fromBody("@john");
        MentionedUsers empty = MentionedUsers.empty();

        // when
        MentionedUsers merged = mentioned.merge(empty);

        // then
        assertThat(merged).isEqualTo(mentioned);
    }

    @Test
    void 동등성을_비교한다() {
        // given
        MentionedUsers mentioned1 = MentionedUsers.fromBody("@john @jane");
        MentionedUsers mentioned2 = MentionedUsers.fromBody("@john @jane");

        // then
        assertThat(mentioned1).isEqualTo(mentioned2);
    }

    @Test
    void null과_병합하면_예외가_발생한다() {
        // given
        MentionedUsers mentioned = MentionedUsers.fromBody("@john");

        // when & then
        assertThatThrownBy(() -> mentioned.merge(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("병합 대상은 null일 수 없습니다.");
    }
}
