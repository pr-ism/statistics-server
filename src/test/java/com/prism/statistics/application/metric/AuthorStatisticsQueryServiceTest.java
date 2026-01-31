package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse.AuthorStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthorStatisticsQueryServiceTest {

    @Autowired
    private AuthorStatisticsQueryService authorStatisticsQueryService;

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_author_statistics.sql")
    @Test
    void 프로젝트의_작성자별_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        AuthorStatisticsResponse actual = authorStatisticsQueryService.findAuthorStatistics(userId, projectId);

        // then

        AuthorStatistics author1 = actual.authorStatistics().stream()
                .filter(a -> a.authorGithubId().equals("author1"))
                .findFirst()
                .orElseThrow();

        AuthorStatistics author2 = actual.authorStatistics().stream()
                .filter(a -> a.authorGithubId().equals("author2"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(actual.authorStatistics()).hasSize(2),
                () -> assertThat(author1.pullRequestCount()).isEqualTo(2),
                () -> assertThat(author1.totalAdditions()).isEqualTo(300),
                () -> assertThat(author1.totalDeletions()).isEqualTo(100),
                () -> assertThat(author1.averageAdditions()).isEqualTo(150.0),
                () -> assertThat(author1.averageDeletions()).isEqualTo(50.0),
                () -> assertThat(author1.averageCommitCount()).isEqualTo(3.0),
                () -> assertThat(author1.averageChangedFileCount()).isEqualTo(4.0),
                () -> assertThat(author2.pullRequestCount()).isEqualTo(2),
                () -> assertThat(author2.totalAdditions()).isEqualTo(200),
                () -> assertThat(author2.totalDeletions()).isEqualTo(40),
                () -> assertThat(author2.averageAdditions()).isEqualTo(100.0),
                () -> assertThat(author2.averageDeletions()).isEqualTo(20.0),
                () -> assertThat(author2.averageCommitCount()).isEqualTo(2.0),
                () -> assertThat(author2.averageChangedFileCount()).isEqualTo(3.0)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PullRequest가_없는_프로젝트의_작성자별_통계를_조회하면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long projectId = 1L;

        // when
        AuthorStatisticsResponse actual = authorStatisticsQueryService.findAuthorStatistics(userId, projectId);

        // then
        assertThat(actual.authorStatistics()).isEmpty();
    }

    @Test
    void 소유하지_않은_프로젝트의_작성자별_통계를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> authorStatisticsQueryService.findAuthorStatistics(userId, projectId))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
