package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.prism.statistics.domain.pullrequest.enums.FileChangeType;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrFileTest {

    @Test
    void PrFile을_생성한다() {
        // when
        PrFile prFile = PrFile.create(
                1L,
                "src/main/java/com/example/Service.java",
                FileChangeType.MODIFIED,
                100,
                50
        );

        // then
        assertAll(
                () -> assertThat(prFile.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(prFile.getFileName()).isEqualTo("src/main/java/com/example/Service.java"),
                () -> assertThat(prFile.getChangeType()).isEqualTo(FileChangeType.MODIFIED),
                () -> assertThat(prFile.getAdditions()).isEqualTo(100),
                () -> assertThat(prFile.getDeletions()).isEqualTo(50)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(null, "file.java", FileChangeType.ADDED, 10, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @Test
    void 파일명이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, null, FileChangeType.ADDED, 10, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명은 필수입니다.");
    }

    @Test
    void 파일명이_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, "  ", FileChangeType.ADDED, 10, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명은 필수입니다.");
    }

    @Test
    void 변경_타입이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, "file.java", null, 10, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 타입은 필수입니다.");
    }

    @Test
    void 추가_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, "file.java", FileChangeType.ADDED, -1, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 삭제_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, "file.java", FileChangeType.ADDED, 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 총_변경_라인_수를_계산한다() {
        // given
        PrFile prFile = PrFile.create(1L, "file.java", FileChangeType.MODIFIED, 100, 50);

        // when
        int totalChanges = prFile.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(150);
    }
}
