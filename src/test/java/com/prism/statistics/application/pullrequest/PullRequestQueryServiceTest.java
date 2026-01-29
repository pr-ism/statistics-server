package com.prism.statistics.application.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestQueryServiceTest {

    @Autowired
    private PullRequestQueryService pullRequestQueryService;

    @Sql("/sql/pullrequest/insert_project_and_pull_requests.sql")
    @Test
    void 프로젝트의_PR_목록을_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        PullRequestListResponse actual = pullRequestQueryService.findAll(userId, projectId);

        // then
        assertThat(actual.pullRequests()).hasSize(3)
                .extracting(pr -> pr.prNumber())
                .containsExactly(30, 20, 10);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PR이_없는_프로젝트의_목록을_조회하면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long projectId = 1L;

        // when
        PullRequestListResponse actual = pullRequestQueryService.findAll(userId, projectId);

        // then
        assertThat(actual.pullRequests()).isEmpty();
    }

    @Sql("/sql/pullrequest/insert_project_and_pull_requests.sql")
    @Test
    void 특정_PR의_상세_정보를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        int prNumber = 20;

        // when
        PullRequestDetailResponse actual = pullRequestQueryService.find(
                userId, projectId, prNumber
        );

        // then
        assertAll(
                () -> assertThat(actual.prNumber()).isEqualTo(20),
                () -> assertThat(actual.title()).isEqualTo("두 번째 PR"),
                () -> assertThat(actual.state()).isEqualTo("MERGED"),
                () -> assertThat(actual.authorGithubId()).isEqualTo("author2"),
                () -> assertThat(actual.commitCount()).isEqualTo(4),
                () -> assertThat(actual.changeStats().changedFileCount()).isEqualTo(5),
                () -> assertThat(actual.changeStats().additionCount()).isEqualTo(100),
                () -> assertThat(actual.changeStats().deletionCount()).isEqualTo(30),
                () -> assertThat(actual.timing().mergedAt()).isNotNull()
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_PR_목록을_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> pullRequestQueryService.findAll(userId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Sql("/sql/pullrequest/insert_project_and_pull_request_with_null_stats.sql")
    @Test
    void changeStats가_null인_PR을_상세_조회하면_예외가_발생한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        int prNumber = 10;

        // when & then
        assertThatThrownBy(() -> pullRequestQueryService.find(userId, projectId, prNumber))
                .isInstanceOf(IllegalStateException.class);
    }

    @Sql("/sql/pullrequest/insert_project_and_pull_request_with_null_timing.sql")
    @Test
    void timing이_null인_PR을_상세_조회하면_예외가_발생한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        int prNumber = 10;

        // when & then
        assertThatThrownBy(() -> pullRequestQueryService.find(userId, projectId, prNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("타이밍 정보");
    }

    @Sql("/sql/pullrequest/insert_project_and_pull_requests.sql")
    @Test
    void 존재하지_않는_PR을_조회하면_예외가_발생한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        int prNumber = 999;

        // when & then
        assertThatThrownBy(() -> pullRequestQueryService.find(userId, projectId, prNumber))
                .isInstanceOf(PullRequestNotFoundException.class);
    }
}
