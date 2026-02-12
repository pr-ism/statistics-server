package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest.LabelData;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestLabelHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestLabelRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
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
class PullRequestLabelAddedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final Long TEST_GITHUB_PULL_REQUEST_ID = 1001L;
    private static final int TEST_PULL_REQUEST_NUMBER = 123;
    private static final String TEST_HEAD_COMMIT_SHA = "abc123";

    @Autowired
    private PullRequestLabelAddedService pullRequestLabelAddedService;

    @Autowired
    private JpaPullRequestLabelRepository jpaPullRequestLabelRepository;

    @Autowired
    private JpaPullRequestLabelHistoryRepository jpaPullRequestLabelHistoryRepository;

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Label_추가_시_PullRequestLabel과_PullRequestLabelHistory가_저장된다() {
        // given
        PullRequestLabelAddedRequest request = createLabelAddedRequest("bug");

        // when
        pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Label_추가_시_PullRequestLabel_정보가_올바르게_저장된다() {
        // given
        String labelName = "enhancement";
        PullRequestLabelAddedRequest request = createLabelAddedRequest(labelName);

        // when
        pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);

        // then
        PullRequestLabel pullRequestLabel = jpaPullRequestLabelRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(pullRequestLabel.getGithubPullRequestId()).isEqualTo(TEST_GITHUB_PULL_REQUEST_ID),
                () -> assertThat(pullRequestLabel.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(pullRequestLabel.getHeadCommitSha()).isEqualTo(TEST_HEAD_COMMIT_SHA),
                () -> assertThat(pullRequestLabel.getLabelName()).isEqualTo(labelName)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Label_추가_시_PullRequestLabelHistory에_ADDED_액션으로_저장된다() {
        // given
        String labelName = "feature";
        PullRequestLabelAddedRequest request = createLabelAddedRequest(labelName);

        // when
        pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);

        // then
        PullRequestLabelHistory pullRequestLabelHistory = jpaPullRequestLabelHistoryRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(pullRequestLabelHistory.getLabelName()).isEqualTo(labelName),
                () -> assertThat(pullRequestLabelHistory.getAction()).isEqualTo(PullRequestLabelAction.ADDED),
                () -> assertThat(pullRequestLabelHistory.getPullRequestId()).isEqualTo(1L)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 중복_Label_추가_시_저장되지_않는다() {
        // given
        String labelName = "duplicate-label";
        PullRequestLabelAddedRequest request = createLabelAddedRequest(labelName);

        // when
        pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);
        pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PullRequestLabelAddedRequest request = createLabelAddedRequest("bug");
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> pullRequestLabelAddedService.addPullRequestLabel(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PullRequest가_없어도_Label이_저장된다() {
        // given
        PullRequestLabelAddedRequest request = createLabelAddedRequest("bug");

        // when
        pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);

        // then
        PullRequestLabel pullRequestLabel = jpaPullRequestLabelRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(pullRequestLabel.getGithubPullRequestId()).isEqualTo(TEST_GITHUB_PULL_REQUEST_ID),
                () -> assertThat(pullRequestLabel.getPullRequestId()).isNull(),
                () -> assertThat(pullRequestLabel.getLabelName()).isEqualTo("bug")
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 동일_Label을_동시에_추가해도_한번만_저장되고_단일_History만_저장된다() throws Exception {
        // given
        String labelName = "concurrent-label";
        PullRequestLabelAddedRequest request = createLabelAddedRequest(labelName);
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
                    pullRequestLabelAddedService.addPullRequestLabel(TEST_API_KEY, request);
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
                () -> assertThat(jpaPullRequestLabelRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestLabelHistoryRepository.count()).isEqualTo(1)
        );
    }

    private PullRequestLabelAddedRequest createLabelAddedRequest(String labelName) {
        return new PullRequestLabelAddedRequest(
                TEST_GITHUB_PULL_REQUEST_ID,
                TEST_PULL_REQUEST_NUMBER,
                TEST_HEAD_COMMIT_SHA,
                new LabelData(labelName),
                Instant.parse("2024-01-15T10:00:00Z")
        );
    }
}
