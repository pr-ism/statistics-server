package com.prism.statistics.domain.pullrequest.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileChangeTypeTest {

    @ParameterizedTest
    @MethodSource("소문자_상태_테스트_데이터")
    void 소문자_상태로_FileChangeType을_생성한다(String status, FileChangeType expected) {
        // when & then
        assertThat(FileChangeType.fromGitHubStatus(status)).isEqualTo(expected);
    }

    static Stream<Arguments> 소문자_상태_테스트_데이터() {
        return Stream.of(
                Arguments.of("modified", FileChangeType.MODIFIED),
                Arguments.of("added", FileChangeType.ADDED),
                Arguments.of("removed", FileChangeType.REMOVED),
                Arguments.of("renamed", FileChangeType.RENAMED)
        );
    }

    @ParameterizedTest
    @MethodSource("대문자_상태_테스트_데이터")
    void 대문자_상태로_FileChangeType을_생성한다(String status, FileChangeType expected) {
        // when & then
        assertThat(FileChangeType.fromGitHubStatus(status)).isEqualTo(expected);
    }

    static Stream<Arguments> 대문자_상태_테스트_데이터() {
        return Stream.of(
                Arguments.of("MODIFIED", FileChangeType.MODIFIED),
                Arguments.of("ADDED", FileChangeType.ADDED),
                Arguments.of("REMOVED", FileChangeType.REMOVED),
                Arguments.of("RENAMED", FileChangeType.RENAMED)
        );
    }

    @ParameterizedTest
    @MethodSource("대소문자_혼합_상태_테스트_데이터")
    void 대소문자_혼합_상태로_FileChangeType을_생성한다(String status, FileChangeType expected) {
        // when & then
        assertThat(FileChangeType.fromGitHubStatus(status)).isEqualTo(expected);
    }

    static Stream<Arguments> 대소문자_혼합_상태_테스트_데이터() {
        return Stream.of(
                Arguments.of("Modified", FileChangeType.MODIFIED),
                Arguments.of("Added", FileChangeType.ADDED),
                Arguments.of("Removed", FileChangeType.REMOVED),
                Arguments.of("Renamed", FileChangeType.RENAMED)
        );
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
