package com.prism.statistics.application.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.dto.request.ReviewerAddedRequest;
import com.prism.statistics.application.webhook.dto.request.ReviewerAddedRequest.ReviewerData;
import com.prism.statistics.domain.reviewer.RequestedReviewer;
import com.prism.statistics.domain.reviewer.RequestedReviewerChangeHistory;
import com.prism.statistics.domain.reviewer.enums.ReviewerAction;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import com.prism.statistics.infrastructure.reviewer.persistence.JpaRequestedReviewerChangeHistoryRepository;
import com.prism.statistics.infrastructure.reviewer.persistence.JpaRequestedReviewerRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewerAddedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PR_NUMBER = 123;
    private static final Instant TEST_REQUESTED_AT = Instant.parse("2024-01-15T10:00:00Z");
    private static final LocalDateTime EXPECTED_REQUESTED_AT = LocalDateTime.of(2024, 1, 15, 19, 0, 0);

    @Autowired
    private ReviewerAddedService reviewerAddedService;

    @Autowired
    private JpaRequestedReviewerRepository jpaRequestedReviewerRepository;

    @Autowired
    private JpaRequestedReviewerChangeHistoryRepository jpaRequestedReviewerChangeHistoryRepository;

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 리뷰어_추가_시_RequestedReviewer와_History가_저장된다() {
        // given
        ReviewerAddedRequest request = createReviewerAddedRequest("reviewer1", 12345L);

        // when
        reviewerAddedService.addReviewer(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(1),
                () -> assertThat(jpaRequestedReviewerChangeHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 리뷰어_추가_시_RequestedReviewer_정보가_올바르게_저장된다() {
        // given
        String githubMention = "reviewer1";
        Long githubUid = 12345L;
        ReviewerAddedRequest request = createReviewerAddedRequest(githubMention, githubUid);

        // when
        reviewerAddedService.addReviewer(TEST_API_KEY, request);

        // then
        List<RequestedReviewer> reviewers = jpaRequestedReviewerRepository.findAll();
        assertThat(reviewers).hasSize(1);

        RequestedReviewer requestedReviewer = reviewers.get(0);
        assertAll(
                () -> assertThat(requestedReviewer.getGithubMention()).isEqualTo(githubMention),
                () -> assertThat(requestedReviewer.getGithubUid()).isEqualTo(githubUid),
                () -> assertThat(requestedReviewer.getRequestedAt()).isEqualTo(EXPECTED_REQUESTED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 리뷰어_추가_시_History에_REQUESTED_액션으로_저장된다() {
        // given
        String githubMention = "reviewer1";
        Long githubUid = 12345L;
        ReviewerAddedRequest request = createReviewerAddedRequest(githubMention, githubUid);

        // when
        reviewerAddedService.addReviewer(TEST_API_KEY, request);

        // then
        List<RequestedReviewerChangeHistory> histories = jpaRequestedReviewerChangeHistoryRepository.findAll();
        assertThat(histories).hasSize(1);

        RequestedReviewerChangeHistory history = histories.get(0);
        assertAll(
                () -> assertThat(history.getGithubMention()).isEqualTo(githubMention),
                () -> assertThat(history.getGithubUid()).isEqualTo(githubUid),
                () -> assertThat(history.getAction()).isEqualTo(ReviewerAction.REQUESTED),
                () -> assertThat(history.getChangedAt()).isEqualTo(EXPECTED_REQUESTED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 중복_리뷰어_추가_시_저장되지_않는다() {
        // given
        ReviewerAddedRequest request = createReviewerAddedRequest("reviewer1", 12345L);

        // when
        reviewerAddedService.addReviewer(TEST_API_KEY, request);
        reviewerAddedService.addReviewer(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(1),
                () -> assertThat(jpaRequestedReviewerChangeHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        ReviewerAddedRequest request = createReviewerAddedRequest("reviewer1", 12345L);
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> reviewerAddedService.addReviewer(invalidApiKey, request))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_PR이면_예외가_발생한다() {
        // given
        ReviewerAddedRequest request = createReviewerAddedRequest("reviewer1", 12345L);

        // when & then
        assertThatThrownBy(() -> reviewerAddedService.addReviewer(TEST_API_KEY, request))
                .isInstanceOf(PullRequestNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 동일_리뷰어를_동시에_추가해도_한번만_저장되고_단일_History만_저장된다() throws Exception {
        // given
        String githubMention = "concurrent-reviewer";
        Long githubUid = 99999L;
        ReviewerAddedRequest request = createReviewerAddedRequest(githubMention, githubUid);
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
                    reviewerAddedService.addReviewer(TEST_API_KEY, request);
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
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(1),
                () -> assertThat(jpaRequestedReviewerChangeHistoryRepository.count()).isEqualTo(1)
        );
    }

    private ReviewerAddedRequest createReviewerAddedRequest(String login, Long id) {
        return new ReviewerAddedRequest(
                TEST_PR_NUMBER,
                new ReviewerData(login, id),
                TEST_REQUESTED_AT
        );
    }
}
