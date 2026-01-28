package com.prism.statistics.application.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.dto.request.LabelRemovedRequest;
import com.prism.statistics.application.webhook.dto.request.LabelRemovedRequest.LabelData;
import com.prism.statistics.domain.label.enums.LabelAction;
import com.prism.statistics.infrastructure.label.persistence.JpaPrLabelHistoryRepository;
import com.prism.statistics.infrastructure.label.persistence.JpaPrLabelRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LabelRemovedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PR_NUMBER = 123;

    @Autowired
    private LabelRemovedService labelRemovedService;

    @Autowired
    private JpaPrLabelRepository jpaPrLabelRepository;

    @Autowired
    private JpaPrLabelHistoryRepository jpaPrLabelHistoryRepository;

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void Label_삭제_시_PrLabel이_삭제되고_PrLabelHistory가_저장된다() {
        // given
        LabelRemovedRequest request = createLabelRemovedRequest("bug");

        // when
        labelRemovedService.removeLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPrLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void Label_삭제_시_PrLabelHistory에_REMOVED_액션으로_저장된다() {
        // given
        String labelName = "bug";
        LabelRemovedRequest request = createLabelRemovedRequest(labelName);

        // when
        labelRemovedService.removeLabel(TEST_API_KEY, request);

        // then
        var prLabelHistory = jpaPrLabelHistoryRepository.findAll().iterator().next();
        assertAll(
                () -> assertThat(prLabelHistory.getLabelName()).isEqualTo(labelName),
                () -> assertThat(prLabelHistory.getAction()).isEqualTo(LabelAction.REMOVED)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 존재하지_않는_Label_삭제_시_아무것도_저장되지_않는다() {
        // given
        LabelRemovedRequest request = createLabelRemovedRequest("non-existent-label");

        // when
        labelRemovedService.removeLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPrLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrLabelHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void 중복_Label_삭제_시_History가_한번만_저장된다() {
        // given
        LabelRemovedRequest request = createLabelRemovedRequest("bug");

        // when
        labelRemovedService.removeLabel(TEST_API_KEY, request);
        labelRemovedService.removeLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPrLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        LabelRemovedRequest request = createLabelRemovedRequest("bug");
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> labelRemovedService.removeLabel(invalidApiKey, request))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_PR이면_예외가_발생한다() {
        // given
        LabelRemovedRequest request = createLabelRemovedRequest("bug");

        // when & then
        assertThatThrownBy(() -> labelRemovedService.removeLabel(TEST_API_KEY, request))
                .isInstanceOf(PullRequestNotFoundException.class);
    }

    private LabelRemovedRequest createLabelRemovedRequest(String labelName) {
        return new LabelRemovedRequest(
                "owner/repo",
                TEST_PR_NUMBER,
                new LabelData(labelName),
                Instant.parse("2024-01-15T10:00:00Z")
        );
    }
}
