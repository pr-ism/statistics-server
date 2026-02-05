package com.prism.statistics.domain.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestOpenedFileChangeTest {

    @Test
    void pull_request_오픈_파일_변경_항목을_생성한다() {
        // given
        Long pullRequestId = 1L;
        FileChangeType changeType = FileChangeType.MODIFIED;
        int count = 3;
        BigDecimal ratio = new BigDecimal("0.75");
        // when
        PullRequestOpenedFileChange fileChange = PullRequestOpenedFileChange.create(
                pullRequestId,
                changeType,
                count,
                ratio
        );

        // then
        assertAll(
                () -> assertThat(fileChange.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(fileChange.getChangeType()).isEqualTo(changeType),
                () -> assertThat(fileChange.getCount()).isEqualTo(count),
                () -> assertThat(fileChange.getRatio()).isEqualTo(ratio)
        );
    }

    @Test
    void pull_request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedFileChange.create(null, FileChangeType.MODIFIED, 1, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Pull Request ID는 필수입니다.")
        );
    }

    @Test
    void 파일_변경_타입이_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedFileChange.create(1L, null, 1, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 변경 타입은 필수입니다.")
        );
    }

    @Test
    void 파일_변경_수가_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedFileChange.create(1L, FileChangeType.MODIFIED, -1, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 변경 수는 0보다 작을 수 없습니다.")
        );
    }

    @Test
    void 파일_변경_비율이_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedFileChange.create(1L, FileChangeType.MODIFIED, 1, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 변경 비율은 필수입니다.")
        );
    }

    @Test
    void 파일_변경_비율이_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedFileChange.create(1L, FileChangeType.MODIFIED, 1, new BigDecimal("-0.01")))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 변경 비율은 0보다 작을 수 없습니다.")
        );
    }

    @Test
    void 파일_변경_비율이_1을_초과하면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedFileChange.create(1L, FileChangeType.MODIFIED, 1, new BigDecimal("1.01")))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 변경 비율은 1을 초과할 수 없습니다.")
        );
    }
}
