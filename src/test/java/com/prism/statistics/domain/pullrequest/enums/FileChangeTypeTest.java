package com.prism.statistics.domain.pullrequest.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileChangeTypeTest {

    @ParameterizedTest
    @CsvSource({
            "modified, MODIFIED",
            "added, ADDED",
            "removed, REMOVED",
            "renamed, RENAMED"
    })
    void 소문자_상태로_FileChangeType을_생성한다(String status, FileChangeType expected) {
        // when
        FileChangeType actual = FileChangeType.fromGitHubStatus(status);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "MODIFIED, MODIFIED",
            "ADDED, ADDED",
            "REMOVED, REMOVED",
            "RENAMED, RENAMED"
    })
    void 대문자_상태로_FileChangeType을_생성한다(String status, FileChangeType expected) {
        // when
        FileChangeType actual = FileChangeType.fromGitHubStatus(status);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "Modified, MODIFIED",
            "Added, ADDED",
            "Removed, REMOVED",
            "Renamed, RENAMED"
    })
    void 대소문자_혼합_상태로_FileChangeType을_생성한다(String status, FileChangeType expected) {
        // when
        FileChangeType actual = FileChangeType.fromGitHubStatus(status);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 알_수_없는_상태이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> FileChangeType.fromGitHubStatus("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("알 수 없는 파일 변경 타입입니다: unknown");
    }

    @Test
    void null_상태이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> FileChangeType.fromGitHubStatus(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
