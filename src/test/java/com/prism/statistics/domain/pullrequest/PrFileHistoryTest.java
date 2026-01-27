package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.vo.FileChanges;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrFileHistoryTest {

    private static final FileChanges FILE_CHANGES = FileChanges.create(100, 50);
    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void PrFileHistory를_생성한다() {
        // when
        PrFileHistory history = PrFileHistory.create(
                1L,
                "src/main/java/com/example/Service.java",
                FileChangeType.MODIFIED,
                FILE_CHANGES,
                CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getFileName()).isEqualTo("src/main/java/com/example/Service.java"),
                () -> assertThat(history.getPreviousFileName()).isNull(),
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.MODIFIED),
                () -> assertThat(history.getFileChanges()).isEqualTo(FILE_CHANGES),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT),
                () -> assertThat(history.isRenamed()).isFalse()
        );
    }

    @Test
    void RENAMED_파일_이력을_생성한다() {
        // when
        PrFileHistory history = PrFileHistory.createRenamed(
                1L,
                "NewName.java",
                "OldName.java",
                FILE_CHANGES,
                CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getFileName()).isEqualTo("NewName.java"),
                () -> assertThat(history.getPreviousFileName()).isEqualTo("OldName.java"),
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.RENAMED),
                () -> assertThat(history.getFileChanges()).isEqualTo(FILE_CHANGES),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT),
                () -> assertThat(history.isRenamed()).isTrue()
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFileHistory.create(null, "file.java", FileChangeType.ADDED, FILE_CHANGES, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 파일명이_null이거나_빈_문자열이면_예외가_발생한다(String fileName) {
        // when & then
        assertThatThrownBy(() -> PrFileHistory.create(1L, fileName, FileChangeType.ADDED, FILE_CHANGES, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명은 필수입니다.");
    }

    @Test
    void 변경_타입이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFileHistory.create(1L, "file.java", null, FILE_CHANGES, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 타입은 필수입니다.");
    }

    @Test
    void create에서_RENAMED_타입도_생성할_수_있다() {
        // when
        PrFileHistory history = PrFileHistory.create(1L, "file.java", FileChangeType.RENAMED, FILE_CHANGES, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.RENAMED),
                () -> assertThat(history.getPreviousFileName()).isNull(),
                () -> assertThat(history.isRenamed()).isFalse()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createRenamed에서_이전_파일명이_null이거나_빈_문자열이면_예외가_발생한다(String previousFileName) {
        // when & then
        assertThatThrownBy(() -> PrFileHistory.createRenamed(1L, "NewName.java", previousFileName, FILE_CHANGES, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 파일명은 필수입니다.");
    }

    @Test
    void 파일_변경_정보가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFileHistory.create(1L, "file.java", FileChangeType.ADDED, null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 정보는 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrFileHistory.create(1L, "file.java", FileChangeType.ADDED, FILE_CHANGES, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }

    @Test
    void 총_변경_라인_수를_계산한다() {
        // given
        PrFileHistory history = PrFileHistory.create(1L, "file.java", FileChangeType.MODIFIED, FILE_CHANGES, CHANGED_AT);

        // when
        int totalChanges = history.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(150);
    }
}
