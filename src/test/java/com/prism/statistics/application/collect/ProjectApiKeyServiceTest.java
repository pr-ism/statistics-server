package com.prism.statistics.application.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectApiKeyServiceTest {

    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectApiKeyService projectApiKeyService;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 유효한_API_Key로_프로젝트_ID를_조회한다() {
        // when
        Long projectId = projectApiKeyService.resolveProjectId(TEST_API_KEY);

        // then
        assertThat(projectId).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_API_Key로_프로젝트_ID를_조회하면_예외가_발생한다() {
        // given
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> projectApiKeyService.resolveProjectId(invalidApiKey))
                .isInstanceOf(InvalidApiKeyException.class);
    }

}
