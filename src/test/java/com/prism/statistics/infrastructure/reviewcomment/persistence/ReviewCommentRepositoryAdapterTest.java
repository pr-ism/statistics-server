package com.prism.statistics.infrastructure.reviewcomment.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.reviewcomment.ReviewComment;
import com.prism.statistics.domain.reviewcomment.enums.CommentSide;
import com.prism.statistics.domain.reviewcomment.vo.CommentLineRange;
import com.prism.statistics.domain.reviewcomment.vo.ParentCommentId;
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
class ReviewCommentRepositoryAdapterTest {

    @Autowired
    private ReviewCommentRepositoryAdapter reviewCommentRepositoryAdapter;

    @Autowired
    private JpaReviewCommentRepository jpaReviewCommentRepository;

    @Test
    void 리뷰_댓글을_저장한다() {
        // given
        ReviewComment reviewComment = createReviewComment(1L);

        // when
        ReviewComment saved = reviewCommentRepositoryAdapter.saveOrFind(reviewComment);

        // then
        assertAll(
                () -> assertThat(saved).isNotNull(),
                () -> assertThat(jpaReviewCommentRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 중복된_github_comment_id로_저장하면_기존_ReviewComment를_반환한다() {
        // given
        Long sameGithubCommentId = 100L;
        ReviewComment firstComment = createReviewComment(sameGithubCommentId);
        ReviewComment duplicateComment = createReviewComment(sameGithubCommentId);

        // when
        ReviewComment firstSaved = reviewCommentRepositoryAdapter.saveOrFind(firstComment);
        ReviewComment duplicateSaved = reviewCommentRepositoryAdapter.saveOrFind(duplicateComment);

        // then
        assertAll(
                () -> assertThat(firstSaved).isNotNull(),
                () -> assertThat(duplicateSaved).isNotNull(),
                () -> assertThat(duplicateSaved.getId()).isEqualTo(firstSaved.getId()),
                () -> assertThat(jpaReviewCommentRepository.count()).isEqualTo(1)
        );
    }

    @Test
    void 동시에_같은_github_comment_id로_저장하면_모두_같은_ReviewComment를_반환한다() throws Exception {
        // given
        Long sameGithubCommentId = 200L;
        int requestCount = 10;

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<ReviewComment>> futures = new ArrayList<>();

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
                    ReviewComment reviewComment = createReviewComment(sameGithubCommentId);
                    return reviewCommentRepositoryAdapter.saveOrFind(reviewComment);
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            List<ReviewComment> results = new ArrayList<>();
            for (Future<ReviewComment> future : futures) {
                ReviewComment result = future.get(5, TimeUnit.SECONDS);
                results.add(result);
            }

            // then
            Long savedCommentId = results.get(0).getId();
            assertAll(
                    () -> assertThat(results).hasSize(requestCount),
                    () -> assertThat(results).allMatch(r -> r.getId().equals(savedCommentId)),
                    () -> assertThat(jpaReviewCommentRepository.count()).isEqualTo(1)
            );
        }
    }

    @Test
    void 서로_다른_github_comment_id는_모두_저장된다() {
        // given
        ReviewComment comment1 = createReviewComment(301L);
        ReviewComment comment2 = createReviewComment(302L);
        ReviewComment comment3 = createReviewComment(303L);

        // when
        ReviewComment saved1 = reviewCommentRepositoryAdapter.saveOrFind(comment1);
        ReviewComment saved2 = reviewCommentRepositoryAdapter.saveOrFind(comment2);
        ReviewComment saved3 = reviewCommentRepositoryAdapter.saveOrFind(comment3);

        // then
        assertAll(
                () -> assertThat(saved1).isNotNull(),
                () -> assertThat(saved2).isNotNull(),
                () -> assertThat(saved3).isNotNull(),
                () -> assertThat(jpaReviewCommentRepository.count()).isEqualTo(3)
        );
    }

    @Test
    void 존재하는_github_comment_id는_true를_반환한다() {
        // given
        Long githubCommentId = 400L;
        ReviewComment comment = createReviewComment(githubCommentId);
        reviewCommentRepositoryAdapter.saveOrFind(comment);

        // when
        boolean exists = reviewCommentRepositoryAdapter.existsByGithubCommentId(githubCommentId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 존재하지_않는_github_comment_id는_false를_반환한다() {
        // given
        Long nonExistentId = 999L;

        // when
        boolean exists = reviewCommentRepositoryAdapter.existsByGithubCommentId(nonExistentId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 동시에_다른_updatedAt으로_수정하면_최신_값이_반영된다() throws Exception {
        // given
        Long githubCommentId = 600L;
        LocalDateTime initialTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime olderTime = LocalDateTime.of(2024, 1, 15, 11, 0);
        LocalDateTime newerTime = LocalDateTime.of(2024, 1, 15, 12, 0);

        ReviewComment comment = createReviewComment(githubCommentId, initialTime);
        reviewCommentRepositoryAdapter.saveOrFind(comment);

        int requestCount = 10;
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(requestCount)) {
            for (int i = 0; i < requestCount; i++) {
                LocalDateTime updateTime = (i % 2 == 0) ? olderTime : newerTime;
                String body = (i % 2 == 0) ? "과거 수정" : "최신 수정";

                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();
                    return reviewCommentRepositoryAdapter.updateBodyIfLatest(githubCommentId, body, updateTime);
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            for (Future<Long> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            // then
            ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(githubCommentId).orElseThrow();
            assertAll(
                    () -> assertThat(result.getBody()).isEqualTo("최신 수정"),
                    () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(newerTime)
            );
        }
    }

    @Test
    void 최신_updatedAt이면_soft_delete한다() {
        // given
        Long githubCommentId = 700L;
        LocalDateTime initialTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime deleteTime = LocalDateTime.of(2024, 1, 15, 11, 0);

        ReviewComment comment = createReviewComment(githubCommentId, initialTime);
        reviewCommentRepositoryAdapter.saveOrFind(comment);

        // when
        long actual = reviewCommentRepositoryAdapter.softDeleteIfLatest(githubCommentId, deleteTime);

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(githubCommentId).orElseThrow();
        assertAll(
                () -> assertThat(actual).isEqualTo(1L),
                () -> assertThat(result.isDeleted()).isTrue(),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(deleteTime)
        );
    }

    @Test
    void 과거_updatedAt이면_soft_delete하지_않는다() {
        // given
        Long githubCommentId = 701L;
        LocalDateTime initialTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime olderTime = LocalDateTime.of(2024, 1, 15, 9, 0);

        ReviewComment comment = createReviewComment(githubCommentId, initialTime);
        reviewCommentRepositoryAdapter.saveOrFind(comment);

        // when
        long actual = reviewCommentRepositoryAdapter.softDeleteIfLatest(githubCommentId, olderTime);

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(githubCommentId).orElseThrow();
        assertAll(
                () -> assertThat(actual).isEqualTo(0L),
                () -> assertThat(result.isDeleted()).isFalse(),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(initialTime)
        );
    }

    @Test
    void 이미_삭제된_댓글은_다시_삭제되지_않는다() {
        // given
        Long githubCommentId = 702L;
        LocalDateTime initialTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime firstDeleteTime = LocalDateTime.of(2024, 1, 15, 11, 0);
        LocalDateTime secondDeleteTime = LocalDateTime.of(2024, 1, 15, 12, 0);

        ReviewComment comment = createReviewComment(githubCommentId, initialTime);
        reviewCommentRepositoryAdapter.saveOrFind(comment);
        reviewCommentRepositoryAdapter.softDeleteIfLatest(githubCommentId, firstDeleteTime);

        // when
        long actual = reviewCommentRepositoryAdapter.softDeleteIfLatest(githubCommentId, secondDeleteTime);

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(githubCommentId).orElseThrow();
        assertAll(
                () -> assertThat(actual).isEqualTo(0L),
                () -> assertThat(result.isDeleted()).isTrue(),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(firstDeleteTime)
        );
    }

    @Test
    void 동시에_같은_댓글을_soft_delete하면_하나만_반영된다() throws Exception {
        // given
        Long githubCommentId = 800L;
        LocalDateTime initialTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime deleteTime = LocalDateTime.of(2024, 1, 15, 12, 0);

        ReviewComment comment = createReviewComment(githubCommentId, initialTime);
        reviewCommentRepositoryAdapter.saveOrFind(comment);

        int requestCount = 10;
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(requestCount)) {
            for (int i = 0; i < requestCount; i++) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();
                    return reviewCommentRepositoryAdapter.softDeleteIfLatest(githubCommentId, deleteTime);
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            long totalUpdated = 0L;
            for (Future<Long> future : futures) {
                totalUpdated += future.get(5, TimeUnit.SECONDS);
            }

            // then
            ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(githubCommentId).orElseThrow();
            long finalTotalUpdated = totalUpdated;
            assertAll(
                    () -> assertThat(finalTotalUpdated).isEqualTo(1L),
                    () -> assertThat(result.isDeleted()).isTrue(),
                    () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(deleteTime)
            );
        }
    }

    private ReviewComment createReviewComment(Long githubCommentId) {
        LocalDateTime githubCreatedAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        return createReviewComment(githubCommentId, githubCreatedAt);
    }

    private ReviewComment createReviewComment(Long githubCommentId, LocalDateTime githubTime) {
        return ReviewComment.builder()
                .githubCommentId(githubCommentId)
                .githubReviewId(100L)
                .body("코드 리뷰 댓글입니다.")
                .path("src/main/java/Example.java")
                .lineRange(CommentLineRange.create(null, 10))
                .side(CommentSide.RIGHT)
                .commitSha("abc123sha")
                .parentCommentId(ParentCommentId.create(null))
                .authorMention("reviewer")
                .authorGithubUid(12345L)
                .githubCreatedAt(githubTime)
                .githubUpdatedAt(githubTime)
                .deleted(false)
                .build();
    }
}
