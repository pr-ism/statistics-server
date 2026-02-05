package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest.LabelData;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestLabelHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestLabelRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestNotFoundException;
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
import java.util.concurrent.TimeUnit;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestLabelRemovedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PULL_REQUEST_NUMBER = 123;

    @Autowired
    private PullRequestLabelRemovedService pullRequestLabelRemovedService;

    @Autowired
    private JpaPullRequestLabelRepository jpaPullRequestLabelRepository;

    @Autowired
    private JpaPullRequestLabelHistoryRepository jpaPullRequestLabelHistoryRepository;

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void Label_삭제_시_PullRequestLabel이_삭제되고_PullRequestLabelHistory가_저장된다() {
        // given
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest("bug");

        // when
        pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void Label_삭제_시_PullRequestLabelHistory에_REMOVED_액션으로_저장된다() {
        // given
        String labelName = "bug";
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest(labelName);

        // when
        pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request);

        // then
        PullRequestLabelHistory pullRequestLabelHistory = jpaPullRequestLabelHistoryRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(pullRequestLabelHistory.getLabelName()).isEqualTo(labelName),
                () -> assertThat(pullRequestLabelHistory.getAction()).isEqualTo(PullRequestLabelAction.REMOVED)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 존재하지_않는_Label_삭제_시_아무것도_저장되지_않는다() {
        // given
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest("non-existent-label");

        // when
        pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void 중복_Label_삭제_시_History가_한번만_저장된다() {
        // given
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest("bug");

        // when
        pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request);
        pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest("bug");
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> pullRequestLabelRemovedService.removePullRequestLabel(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_PullRequest면_예외가_발생한다() {
        // given
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest("bug");

        // when & then
        assertThatThrownBy(() -> pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request))
                .isInstanceOf(PullRequestNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project_pr_and_label.sql")
    @Test
    void 동일_Label을_동시에_삭제해도_한번만_삭제되고_단일_History만_저장된다() throws Exception {
        // given
        String labelName = "bug";
        PullRequestLabelRemovedRequest request = createLabelRemovedRequest(labelName);
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
                    pullRequestLabelRemovedService.removePullRequestLabel(TEST_API_KEY, request);
                    return null;
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            for (Future<Void> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        }

        // then
        assertAll(
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    private PullRequestLabelRemovedRequest createLabelRemovedRequest(String labelName) {
        return new PullRequestLabelRemovedRequest(
                TEST_PULL_REQUEST_NUMBER,
                new LabelData(labelName),
                Instant.parse("2024-01-15T10:00:00Z")
        );
    }
}
