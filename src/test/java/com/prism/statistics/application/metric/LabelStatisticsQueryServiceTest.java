package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.request.LabelStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse.LabelStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.time.LocalDate;
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
        LabelStatisticsRequest request = new LabelStatisticsRequest(null, null);

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId, request);

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
        LabelStatisticsRequest request = new LabelStatisticsRequest(null, null);

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId, request);

        // then
        assertThat(actual.labelStatistics()).isEmpty();
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_label_statistics_date_range.sql")
    @Test
    void 시작일과_종료일을_지정하면_해당_범위의_라벨_통계만_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 2, 1);
        LabelStatisticsRequest request = new LabelStatisticsRequest(startDate, endDate);

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId, request);

        // then
        // PR2(Jan 15, bug) + PR3(Feb 1, bug) 포함, PR1(Jan 10) 및 PR4(Mar 1) 제외
        assertAll(
                () -> assertThat(actual.labelStatistics()).hasSize(1),
                () -> assertThat(actual.labelStatistics())
                        .extracting(LabelStatistics::labelName)
                        .containsExactly("bug"),
                () -> assertThat(actual.labelStatistics().get(0).pullRequestCount()).isEqualTo(2),
                () -> assertThat(actual.labelStatistics().get(0).totalAdditions()).isEqualTo(200),
                () -> assertThat(actual.labelStatistics().get(0).totalDeletions()).isEqualTo(80),
                () -> assertThat(actual.labelStatistics().get(0).averageAdditions()).isEqualTo(100.0),
                () -> assertThat(actual.labelStatistics().get(0).averageDeletions()).isEqualTo(40.0),
                () -> assertThat(actual.labelStatistics().get(0).averageCommitCount()).isEqualTo(2.0),
                () -> assertThat(actual.labelStatistics().get(0).averageChangedFileCount()).isEqualTo(3.0)
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_label_statistics_date_range.sql")
    @Test
    void 시작일만_지정하면_시작일_이후의_라벨_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate startDate = LocalDate.of(2024, 2, 1);
        LabelStatisticsRequest request = new LabelStatisticsRequest(startDate, null);

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId, request);

        // then
        // PR3(Feb 1, bug) + PR4(Mar 1, refactor) 포함, PR1(Jan 10) 및 PR2(Jan 15) 제외
        assertAll(
                () -> assertThat(actual.labelStatistics()).hasSize(2),
                () -> assertThat(actual.labelStatistics())
                        .extracting(LabelStatistics::labelName)
                        .containsExactlyInAnyOrder("bug", "refactor"),
                () -> {
                    LabelStatistics bug = actual.labelStatistics().stream()
                            .filter(l -> l.labelName().equals("bug"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(bug.pullRequestCount()).isEqualTo(1);
                    assertThat(bug.totalAdditions()).isEqualTo(150);
                    assertThat(bug.totalDeletions()).isEqualTo(60);
                },
                () -> {
                    LabelStatistics refactor = actual.labelStatistics().stream()
                            .filter(l -> l.labelName().equals("refactor"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(refactor.pullRequestCount()).isEqualTo(1);
                    assertThat(refactor.totalAdditions()).isEqualTo(200);
                    assertThat(refactor.totalDeletions()).isEqualTo(180);
                }
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_label_statistics_date_range.sql")
    @Test
    void 종료일만_지정하면_종료일_이전의_라벨_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate endDate = LocalDate.of(2024, 1, 15);
        LabelStatisticsRequest request = new LabelStatisticsRequest(null, endDate);

        // when
        LabelStatisticsResponse actual = labelStatisticsQueryService.findLabelStatistics(userId, projectId, request);

        // then
        // PR1(Jan 10, feature+enhancement) + PR2(Jan 15, bug) 포함, PR3(Feb 1) 및 PR4(Mar 1) 제외
        assertAll(
                () -> assertThat(actual.labelStatistics()).hasSize(3),
                () -> assertThat(actual.labelStatistics())
                        .extracting(LabelStatistics::labelName)
                        .containsExactlyInAnyOrder("bug", "feature", "enhancement")
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_라벨별_통계를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;
        LabelStatisticsRequest request = new LabelStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() -> labelStatisticsQueryService.findLabelStatistics(userId, projectId, request))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
