package com.prism.statistics.application.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.dto.request.LabelAddedRequest;
import com.prism.statistics.application.webhook.dto.request.LabelAddedRequest.LabelData;
import com.prism.statistics.domain.label.PrLabel;
import com.prism.statistics.domain.label.PrLabelHistory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LabelAddedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PR_NUMBER = 123;

    @Autowired
    private LabelAddedService labelAddedService;

    @Autowired
    private JpaPrLabelRepository jpaPrLabelRepository;

    @Autowired
    private JpaPrLabelHistoryRepository jpaPrLabelHistoryRepository;

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Label_추가_시_PrLabel과_PrLabelHistory가_저장된다() {
        // given
        LabelAddedRequest request = createLabelAddedRequest("bug");

        // when
        labelAddedService.addLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPrLabelRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPrLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Label_추가_시_PrLabel_정보가_올바르게_저장된다() {
        // given
        String labelName = "enhancement";
        LabelAddedRequest request = createLabelAddedRequest(labelName);

        // when
        labelAddedService.addLabel(TEST_API_KEY, request);

        // then
        PrLabel prLabel = jpaPrLabelRepository.findAll().getFirst();
        assertThat(prLabel.getLabelName()).isEqualTo(labelName);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Label_추가_시_PrLabelHistory에_ADDED_액션으로_저장된다() {
        // given
        String labelName = "feature";
        LabelAddedRequest request = createLabelAddedRequest(labelName);

        // when
        labelAddedService.addLabel(TEST_API_KEY, request);

        // then
        PrLabelHistory prLabelHistory = jpaPrLabelHistoryRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(prLabelHistory.getLabelName()).isEqualTo(labelName),
                () -> assertThat(prLabelHistory.getAction()).isEqualTo(LabelAction.ADDED)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 중복_Label_추가_시_저장되지_않는다() {
        // given
        String labelName = "duplicate-label";
        LabelAddedRequest request = createLabelAddedRequest(labelName);

        // when
        labelAddedService.addLabel(TEST_API_KEY, request);
        labelAddedService.addLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPrLabelRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPrLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        LabelAddedRequest request = createLabelAddedRequest("bug");
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> labelAddedService.addLabel(invalidApiKey, request))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_PR이면_예외가_발생한다() {
        // given
        LabelAddedRequest request = createLabelAddedRequest("bug");

        // when & then
        assertThatThrownBy(() -> labelAddedService.addLabel(TEST_API_KEY, request))
                .isInstanceOf(PullRequestNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 동일_Label을_동시에_추가해도_한번만_저장되고_단일_History만_저장된다() throws Exception {
        // given
        String labelName = "concurrent-label";
        LabelAddedRequest request = createLabelAddedRequest(labelName);
        int requestCount = 10;

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(requestCount)) {
            for (int i = 0; i < requestCount; i++) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("시작 대기 중 인터럽트 발생", e);
                    }
                    // when
                    labelAddedService.addLabel(TEST_API_KEY, request);
                    return null;
                }));
            }

            readyLatch.await();
            startLatch.countDown();

            for (Future<Void> future : futures) {
                future.get();
            }
        }

        // then
        assertAll(
                () -> assertThat(jpaPrLabelRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPrLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    private LabelAddedRequest createLabelAddedRequest(String labelName) {
        return new LabelAddedRequest(
                TEST_PR_NUMBER,
                new LabelData(labelName),
                Instant.parse("2024-01-15T10:00:00Z")
        );
    }
}
