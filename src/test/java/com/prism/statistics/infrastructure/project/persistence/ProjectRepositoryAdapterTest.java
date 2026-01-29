package com.prism.statistics.infrastructure.project.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.prism.statistics.application.IntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectRepositoryAdapterTest {

    @Autowired
    private ProjectRepositoryAdapter projectRepositoryAdapter;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void apiKey로_projectId를_조회한다() {
        // given
        String apiKey = "test-api-key";

        // when
        Optional<Long> actual = projectRepositoryAdapter.findIdByApiKey(apiKey);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_apiKey로_조회하면_빈_Optional을_반환한다() {
        // given
        String invalidApiKey = "invalid-api-key";

        // when
        Optional<Long> actual = projectRepositoryAdapter.findIdByApiKey(invalidApiKey);

        // then
        assertThat(actual).isEmpty();
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 프로젝트_ID와_사용자_ID로_존재_여부를_확인한다() {
        // given
        Long projectId = 1L;
        Long userId = 1L;

        // when
        Optional<Long> actual = projectRepositoryAdapter.existsByIdAndUserId(projectId, userId);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(1L);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 소유하지_않은_프로젝트는_빈_Optional을_반환한다() {
        // given
        Long projectId = 1L;
        Long otherUserId = 999L;

        // when
        Optional<Long> actual = projectRepositoryAdapter.existsByIdAndUserId(projectId, otherUserId);

        // then
        assertThat(actual).isEmpty();
    }
}
