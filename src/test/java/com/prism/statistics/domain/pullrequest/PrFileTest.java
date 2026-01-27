package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.vo.FileChanges;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrFileTest {

    private static final FileChanges FILE_CHANGES = FileChanges.create(100, 50);

    @Test
    void PrFile을_생성한다() {
        // when
        PrFile prFile = PrFile.create(
                1L,
                "src/main/java/com/example/Service.java",
                FileChangeType.MODIFIED,
                FILE_CHANGES
        );

        // then
        assertAll(
                () -> assertThat(prFile.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(prFile.getFileName()).isEqualTo("src/main/java/com/example/Service.java"),
                () -> assertThat(prFile.getChangeType()).isEqualTo(FileChangeType.MODIFIED),
                () -> assertThat(prFile.getFileChanges()).isEqualTo(FILE_CHANGES)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(null, "file.java", FileChangeType.ADDED, FILE_CHANGES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 파일명이_null이거나_빈_문자열이면_예외가_발생한다(String fileName) {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, fileName, FileChangeType.ADDED, FILE_CHANGES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명은 필수입니다.");
    }

    @Test
    void 변경_타입이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, "file.java", null, FILE_CHANGES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 타입은 필수입니다.");
    }

    @Test
    void 파일_변경_정보가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFile.create(1L, "file.java", FileChangeType.ADDED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 정보는 필수입니다.");
    }

    @Test
    void 총_변경_라인_수를_계산한다() {
        // given
        PrFile prFile = PrFile.create(1L, "file.java", FileChangeType.MODIFIED, FILE_CHANGES);

        // when
        int totalChanges = prFile.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(150);
    }
}
