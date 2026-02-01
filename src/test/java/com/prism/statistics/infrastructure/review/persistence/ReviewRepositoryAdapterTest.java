package com.prism.statistics.infrastructure.review.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.review.Review;
import com.prism.statistics.domain.review.enums.ReviewState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewRepositoryAdapterTest {

    @Autowired
    private ReviewRepositoryAdapter reviewRepositoryAdapter;

    @Autowired
    private JpaReviewRepository jpaReviewRepository;

    @Test
    void 리뷰를_저장한다() {
        // given
        Review review = createReview(1L);

        // when
        Review saved = reviewRepositoryAdapter.save(review);

        // then
        assertAll(
                () -> assertThat(saved).isNotNull(),
                () -> assertThat(jpaReviewRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 중복된_github_review_id로_저장하면_null을_반환한다() {
        // given
        Long sameGithubReviewId = 100L;
        Review firstReview = createReview(sameGithubReviewId);
        Review duplicateReview = createReview(sameGithubReviewId);

        // when
        Review firstSaved = reviewRepositoryAdapter.save(firstReview);
        Review duplicateSaved = reviewRepositoryAdapter.save(duplicateReview);

        // then
        assertAll(
                () -> assertThat(firstSaved).isNotNull(),
                () -> assertThat(duplicateSaved).isNull(),
                () -> assertThat(jpaReviewRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 동시에_같은_github_review_id로_저장하면_하나만_저장된다() throws Exception {
        // given
        Long sameGithubReviewId = 200L;
        int requestCount = 10;

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Review>> futures = new ArrayList<>();

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
                    Review review = createReview(sameGithubReviewId);
                    return reviewRepositoryAdapter.save(review);
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            List<Review> results = new ArrayList<>();
            for (Future<Review> future : futures) {
                Review result = future.get(5, TimeUnit.SECONDS);
                if (result != null) {
                    results.add(result);
                }
            }

            // then
            assertAll(
                    () -> assertThat(results).hasSize(1),
                    () -> assertThat(jpaReviewRepository.count()).isEqualTo(1)
            );
        }
    }

    @Test
    void 서로_다른_github_review_id는_모두_저장된다() {
        // given
        Review review1 = createReview(301L);
        Review review2 = createReview(302L);
        Review review3 = createReview(303L);

        // when
        Review saved1 = reviewRepositoryAdapter.save(review1);
        Review saved2 = reviewRepositoryAdapter.save(review2);
        Review saved3 = reviewRepositoryAdapter.save(review3);

        // then
        assertAll(
                () -> assertThat(saved1).isNotNull(),
                () -> assertThat(saved2).isNotNull(),
                () -> assertThat(saved3).isNotNull(),
                () -> assertThat(jpaReviewRepository.count()).isEqualTo(3)
        );
    }

    private Review createReview(Long githubReviewId) {
        return Review.create(
                1L,
                githubReviewId,
                "reviewer",
                12345L,
                ReviewState.APPROVED,
                "abc123sha",
                "LGTM",
                0,
                LocalDateTime.now()
        );
    }
}
