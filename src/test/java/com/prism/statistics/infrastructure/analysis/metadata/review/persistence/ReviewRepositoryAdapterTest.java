package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.ReviewRepositoryAdapter;
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
        Review saved = reviewRepositoryAdapter.saveOrFind(review);

        // then
        assertAll(
                () -> assertThat(saved).isNotNull(),
                () -> assertThat(jpaReviewRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 중복된_github_review_id로_저장하면_기존_Review를_반환한다() {
        // given
        Long sameGithubReviewId = 100L;
        Review firstReview = createReview(sameGithubReviewId);
        Review duplicateReview = createReview(sameGithubReviewId);

        // when
        Review firstSaved = reviewRepositoryAdapter.saveOrFind(firstReview);
        Review duplicateSaved = reviewRepositoryAdapter.saveOrFind(duplicateReview);

        // then
        assertAll(
                () -> assertThat(firstSaved).isNotNull(),
                () -> assertThat(duplicateSaved).isNotNull(),
                () -> assertThat(duplicateSaved.getId()).isEqualTo(firstSaved.getId()),
                () -> assertThat(jpaReviewRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 동시에_같은_github_review_id로_저장하면_모두_같은_Review를_반환한다() throws Exception {
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
                    return reviewRepositoryAdapter.saveOrFind(review);
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            List<Review> results = new ArrayList<>();
            for (Future<Review> future : futures) {
                Review result = future.get(5, TimeUnit.SECONDS);
                results.add(result);
            }

            // then
            Long savedReviewId = results.get(0).getId();
            assertAll(
                    () -> assertThat(results).hasSize(requestCount),
                    () -> assertThat(results).allMatch(r -> r.getId().equals(savedReviewId)),
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
        Review saved1 = reviewRepositoryAdapter.saveOrFind(review1);
        Review saved2 = reviewRepositoryAdapter.saveOrFind(review2);
        Review saved3 = reviewRepositoryAdapter.saveOrFind(review3);

        // then
        assertAll(
                () -> assertThat(saved1).isNotNull(),
                () -> assertThat(saved2).isNotNull(),
                () -> assertThat(saved3).isNotNull(),
                () -> assertThat(jpaReviewRepository.count()).isEqualTo(3)
        );
    }

    @Test
    void pullRequestId가_없는_리뷰에_backfill한다() {
        // given
        Long githubPullRequestId = 500L;
        Review review1 = createReview(501L, githubPullRequestId);
        Review review2 = createReview(502L, githubPullRequestId);
        reviewRepositoryAdapter.saveOrFind(review1);
        reviewRepositoryAdapter.saveOrFind(review2);

        Long pullRequestId = 1L;

        // when
        long updated = reviewRepositoryAdapter.backfillPullRequestId(githubPullRequestId, pullRequestId);

        // then
        assertAll(
                () -> assertThat(updated).isEqualTo(2L),
                () -> assertThat(jpaReviewRepository.findAll())
                        .allMatch(r -> pullRequestId.equals(r.getPullRequestId()))
        );
    }

    @Test
    void pullRequestId가_이미_있는_리뷰는_backfill하지_않는다() {
        // given
        Long githubPullRequestId = 600L;
        Review review = createReview(601L, githubPullRequestId);
        review.assignPullRequestId(99L);
        reviewRepositoryAdapter.saveOrFind(review);

        // when
        long updated = reviewRepositoryAdapter.backfillPullRequestId(githubPullRequestId, 1L);

        // then
        Review result = jpaReviewRepository.findByGithubReviewId(601L).orElseThrow();
        assertAll(
                () -> assertThat(updated).isEqualTo(0L),
                () -> assertThat(result.getPullRequestId()).isEqualTo(99L)
        );
    }

    private Review createReview(Long githubReviewId) {
        return createReview(githubReviewId, 1L);
    }

    private Review createReview(Long githubReviewId, Long githubPullRequestId) {
        return Review.builder()
                .githubPullRequestId(githubPullRequestId)
                .githubReviewId(githubReviewId)
                .reviewer(GithubUser.create("reviewer", 12345L))
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123sha")
                .body("LGTM")
                .commentCount(0)
                .githubSubmittedAt(LocalDateTime.now())
                .build();
    }
}
