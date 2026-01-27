package com.prism.statistics.domain.pullrequest.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileChangesTest {

    @Test
    void FileChanges를_생성한다() {
        // when
        FileChanges fileChanges = FileChanges.create(100, 50);

        // then
        assertAll(
                () -> assertThat(fileChanges.getAdditions()).isEqualTo(100),
                () -> assertThat(fileChanges.getDeletions()).isEqualTo(50)
        );
    }

    @Test
    void 추가_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> FileChanges.create(-1, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 삭제_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> FileChanges.create(100, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 총_변경_라인_수를_계산한다() {
        // given
        FileChanges fileChanges = FileChanges.create(100, 50);

        // when
        int totalChanges = fileChanges.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(150);
    }

    @Test
    void 동등성을_비교한다() {
        // given
        FileChanges fileChanges1 = FileChanges.create(100, 50);
        FileChanges fileChanges2 = FileChanges.create(100, 50);

        // then
        assertThat(fileChanges1).isEqualTo(fileChanges2);
    }
}
