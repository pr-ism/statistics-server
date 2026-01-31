package com.prism.statistics.application.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.application.webhook.dto.request.ReviewerRemovedRequest.ReviewerData;
import com.prism.statistics.domain.reviewer.RequestedReviewerHistory;
import com.prism.statistics.domain.reviewer.enums.ReviewerAction;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import com.prism.statistics.infrastructure.reviewer.persistence.JpaRequestedReviewerHistoryRepository;
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
import java.util.concurrent.TimeUnit;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewerRemovedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PULL_REQUEST_NUMBER = 123;
    private static final Instant TEST_REMOVED_AT = Instant.parse("2024-01-15T10:00:00Z");
    private static final LocalDateTime EXPECTED_REMOVED_AT = LocalDateTime.of(2024, 1, 15, 19, 0, 0);

    @Autowired
    private ReviewerRemovedService reviewerRemovedService;

    @Autowired
    private JpaRequestedReviewerRepository jpaRequestedReviewerRepository;

    @Autowired
    private JpaRequestedReviewerHistoryRepository jpaRequestedReviewerHistoryRepository;

    @Sql("/sql/webhook/insert_project_pr_and_reviewer.sql")
    @Test
    void 리뷰어_삭제_시_RequestedReviewer가_삭제되고_History가_저장된다() {
        // given
        ReviewerRemovedRequest request = createReviewerRemovedRequest("reviewer1", 12345L);

        // when
        reviewerRemovedService.removeReviewer(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(0),
                () -> assertThat(jpaRequestedReviewerHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_and_reviewer.sql")
    @Test
    void 리뷰어_삭제_시_History에_REMOVED_액션으로_저장된다() {
        // given
        String githubMention = "reviewer1";
        Long githubUid = 12345L;
        ReviewerRemovedRequest request = createReviewerRemovedRequest(githubMention, githubUid);

        // when
        reviewerRemovedService.removeReviewer(TEST_API_KEY, request);

        // then
        List<RequestedReviewerHistory> histories = jpaRequestedReviewerHistoryRepository.findAll();
        assertThat(histories).hasSize(1);

        RequestedReviewerHistory history = histories.get(0);
        assertAll(
                () -> assertThat(history.getGithubMention()).isEqualTo(githubMention),
                () -> assertThat(history.getGithubUid()).isEqualTo(githubUid),
                () -> assertThat(history.getAction()).isEqualTo(ReviewerAction.REMOVED),
                () -> assertThat(history.getChangedAt()).isEqualTo(EXPECTED_REMOVED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 존재하지_않는_리뷰어_삭제_시_아무것도_저장되지_않는다() {
        // given
        ReviewerRemovedRequest request = createReviewerRemovedRequest("non-existent", 99999L);

        // when
        reviewerRemovedService.removeReviewer(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(0),
                () -> assertThat(jpaRequestedReviewerHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_and_reviewer.sql")
    @Test
    void 중복_리뷰어_삭제_시_History가_한번만_저장된다() {
        // given
        ReviewerRemovedRequest request = createReviewerRemovedRequest("reviewer1", 12345L);

        // when
        reviewerRemovedService.removeReviewer(TEST_API_KEY, request);
        reviewerRemovedService.removeReviewer(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(0),
                () -> assertThat(jpaRequestedReviewerHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        ReviewerRemovedRequest request = createReviewerRemovedRequest("reviewer1", 12345L);
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> reviewerRemovedService.removeReviewer(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_PullRequest면_예외가_발생한다() {
        // given
        ReviewerRemovedRequest request = createReviewerRemovedRequest("reviewer1", 12345L);

        // when & then
        assertThatThrownBy(() -> reviewerRemovedService.removeReviewer(TEST_API_KEY, request))
                .isInstanceOf(PullRequestNotFoundException.class);
    }

    @Sql("/sql/webhook/insert_project_pr_and_reviewer.sql")
    @Test
    void 동일_리뷰어를_동시에_삭제해도_한번만_삭제되고_단일_History만_저장된다() throws Exception {
        // given
        String githubMention = "reviewer1";
        Long githubUid = 12345L;
        ReviewerRemovedRequest request = createReviewerRemovedRequest(githubMention, githubUid);
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
                    reviewerRemovedService.removeReviewer(TEST_API_KEY, request);
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
                () -> assertThat(jpaRequestedReviewerRepository.count()).isEqualTo(0),
                () -> assertThat(jpaRequestedReviewerHistoryRepository.count()).isEqualTo(1)
        );
    }

    private ReviewerRemovedRequest createReviewerRemovedRequest(String login, Long id) {
        return new ReviewerRemovedRequest(
                TEST_PULL_REQUEST_NUMBER,
                new ReviewerData(login, id),
                TEST_REMOVED_AT
        );
    }
}
