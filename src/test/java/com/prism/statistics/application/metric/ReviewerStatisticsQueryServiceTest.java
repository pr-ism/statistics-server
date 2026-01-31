package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.response.ReviewerStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.ReviewerStatisticsResponse.ReviewerStatistics;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewerStatisticsQueryServiceTest {

    @Autowired
    private ReviewerStatisticsQueryService reviewerStatisticsQueryService;

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_reviewer_statistics.sql")
    @Test
    void 프로젝트의_리뷰어별_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        ReviewerStatisticsResponse actual = reviewerStatisticsQueryService.findReviewerStatistics(userId, projectId);

        // then

        ReviewerStatistics reviewer1 = actual.reviewerStatistics().stream()
                .filter(r -> r.reviewerGithubMention().equals("reviewer1"))
                .findFirst()
                .orElseThrow();

        ReviewerStatistics reviewer2 = actual.reviewerStatistics().stream()
                .filter(r -> r.reviewerGithubMention().equals("reviewer2"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(actual.reviewerStatistics()).hasSize(2),
                () -> assertThat(reviewer1.reviewCount()).isEqualTo(2),
                () -> assertThat(reviewer1.totalAdditions()).isEqualTo(300),
                () -> assertThat(reviewer1.totalDeletions()).isEqualTo(100),
                () -> assertThat(reviewer1.averageAdditions()).isEqualTo(150.0),
                () -> assertThat(reviewer1.averageDeletions()).isEqualTo(50.0),
                () -> assertThat(reviewer1.averageCommitCount()).isEqualTo(3.0),
                () -> assertThat(reviewer1.averageChangedFileCount()).isEqualTo(4.0),
                () -> assertThat(reviewer2.reviewCount()).isEqualTo(2),
                () -> assertThat(reviewer2.totalAdditions()).isEqualTo(250),
                () -> assertThat(reviewer2.totalDeletions()).isEqualTo(70),
                () -> assertThat(reviewer2.averageAdditions()).isEqualTo(125.0),
                () -> assertThat(reviewer2.averageDeletions()).isEqualTo(35.0),
                () -> assertThat(reviewer2.averageCommitCount()).isEqualTo(2.5),
                () -> assertThat(reviewer2.averageChangedFileCount()).isEqualTo(3.5)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 리뷰어가_없는_프로젝트의_리뷰어별_통계를_조회하면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long projectId = 1L;

        // when
        ReviewerStatisticsResponse actual = reviewerStatisticsQueryService.findReviewerStatistics(userId, projectId);

        // then
        assertThat(actual.reviewerStatistics()).isEmpty();
    }

    @Test
    void 소유하지_않은_프로젝트의_리뷰어별_통계를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> reviewerStatisticsQueryService.findReviewerStatistics(userId, projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
