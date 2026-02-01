package com.prism.statistics.domain.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestSizeCategoryTest {

    @Test
    void 변경량이_0이면_SMALL로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(0)).isEqualTo(PullRequestSizeCategory.SMALL);
    }

    @Test
    void 변경량이_100이면_SMALL로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(100)).isEqualTo(PullRequestSizeCategory.SMALL);
    }

    @Test
    void 변경량이_101이면_MEDIUM으로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(101)).isEqualTo(PullRequestSizeCategory.MEDIUM);
    }

    @Test
    void 변경량이_300이면_MEDIUM으로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(300)).isEqualTo(PullRequestSizeCategory.MEDIUM);
    }

    @Test
    void 변경량이_301이면_LARGE로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(301)).isEqualTo(PullRequestSizeCategory.LARGE);
    }

    @Test
    void 변경량이_700이면_LARGE로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(700)).isEqualTo(PullRequestSizeCategory.LARGE);
    }

    @Test
    void 변경량이_701이면_EXTRA_LARGE로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(701)).isEqualTo(PullRequestSizeCategory.EXTRA_LARGE);
    }

    @Test
    void 변경량이_매우_크면_EXTRA_LARGE로_분류한다() {
        assertThat(PullRequestSizeCategory.classify(10000)).isEqualTo(PullRequestSizeCategory.EXTRA_LARGE);
    }

    @Test
    void 변경량이_음수이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestSizeCategory.classify(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("분류할 수 없는 라인 수입니다");
    }
}
