package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse.LabelStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LabelStatisticsQueryServiceTest {

    @Autowired
    private LabelStatisticsQueryService labelStatisticsQueryService;

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_label_statistics.sql")
    @Test
    void 프로젝트의_라벨별_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId);

        // then

        LabelStatistics bug = actual.labelStatistics().stream()
                .filter(l -> l.labelName().equals("bug"))
                .findFirst()
                .orElseThrow();

        LabelStatistics feature = actual.labelStatistics().stream()
                .filter(l -> l.labelName().equals("feature"))
                .findFirst()
                .orElseThrow();

        LabelStatistics refactor = actual.labelStatistics().stream()
                .filter(l -> l.labelName().equals("refactor"))
                .findFirst()
                .orElseThrow();

        LabelStatistics enhancement = actual.labelStatistics().stream()
                .filter(l -> l.labelName().equals("enhancement"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(actual.labelStatistics())
                        .extracting(singelLabelStatistics -> singelLabelStatistics.labelName())
                        .containsExactlyInAnyOrder("bug", "feature", "refactor", "enhancement"),
                () -> assertThat(bug.pullRequestCount()).isEqualTo(2),
                () -> assertThat(bug.totalAdditions()).isEqualTo(200),
                () -> assertThat(bug.totalDeletions()).isEqualTo(80),
                () -> assertThat(bug.averageAdditions()).isEqualTo(100.0),
                () -> assertThat(bug.averageDeletions()).isEqualTo(40.0),
                () -> assertThat(bug.averageCommitCount()).isEqualTo(2.0),
                () -> assertThat(bug.averageChangedFileCount()).isEqualTo(3.0),
                () -> assertThat(feature.pullRequestCount()).isEqualTo(1),
                () -> assertThat(feature.totalAdditions()).isEqualTo(400),
                () -> assertThat(feature.totalDeletions()).isEqualTo(100),
                () -> assertThat(feature.averageAdditions()).isEqualTo(400.0),
                () -> assertThat(feature.averageDeletions()).isEqualTo(100.0),
                () -> assertThat(feature.averageCommitCount()).isEqualTo(5.0),
                () -> assertThat(feature.averageChangedFileCount()).isEqualTo(8.0),
                () -> assertThat(refactor.pullRequestCount()).isEqualTo(1),
                () -> assertThat(refactor.totalAdditions()).isEqualTo(200),
                () -> assertThat(refactor.totalDeletions()).isEqualTo(180),
                () -> assertThat(enhancement.pullRequestCount()).isEqualTo(1),
                () -> assertThat(enhancement.totalAdditions()).isEqualTo(400),
                () -> assertThat(enhancement.totalDeletions()).isEqualTo(100)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 라벨이_없는_프로젝트의_라벨별_통계를_조회하면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long projectId = 1L;

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId);

        // then
        assertThat(actual.labelStatistics()).isEmpty();
    }

    @Test
    void 소유하지_않은_프로젝트의_라벨별_통계를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> labelStatisticsQueryService.findLabelStatistics(userId, projectId))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
