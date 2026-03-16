package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.collect.inbox.ProcessingSourceContext;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentDeletedRequest;
import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewCommentNotFoundException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewCommentRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewCommentDeletedServiceTest {


    private static final Long EXISTING_COMMENT_ID = 100L;
    private static final String ORIGINAL_BODY = "원본 댓글 내용";
    private static final LocalDateTime ORIGINAL_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
    private static final Instant FIXED_UPDATED_AT = Instant.parse("2024-01-15T02:00:00Z");

    @Autowired
    private ReviewCommentDeletedService reviewCommentDeletedService;

    @Autowired
    private ProcessingSourceContext processingSourceContext;

    @Autowired
    private JpaReviewCommentRepository jpaReviewCommentRepository;

    @Sql("/sql/webhook/insert_project_and_review_comment.sql")
    @Test
    void 최신_updatedAt이면_soft_delete한다() {
        // given
        Instant newerUpdatedAt = Instant.parse("2024-01-15T02:00:00Z"); // KST 11:00
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2024, 1, 15, 11, 0, 0);

        ReviewCommentDeletedRequest request = new ReviewCommentDeletedRequest(
                null,
                EXISTING_COMMENT_ID,
                newerUpdatedAt
        );

        // when
        processingSourceContext.withInboxProcessing(() -> reviewCommentDeletedService.deleteReviewComment(request));

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(EXISTING_COMMENT_ID).orElseThrow();
        assertAll(
                () -> assertThat(result.isDeleted()).isTrue(),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(expectedUpdatedAt),
                () -> assertThat(result.getBody()).isEqualTo(ORIGINAL_BODY)
        );
    }

    @Sql("/sql/webhook/insert_project_and_review_comment.sql")
    @Test
    void 과거_updatedAt이면_삭제하지_않는다() {
        // given
        Instant olderUpdatedAt = Instant.parse("2024-01-15T00:00:00Z"); // KST 09:00 (기존 10:00보다 과거)

        ReviewCommentDeletedRequest request = new ReviewCommentDeletedRequest(
                null,
                EXISTING_COMMENT_ID,
                olderUpdatedAt
        );

        // when
        processingSourceContext.withInboxProcessing(() -> reviewCommentDeletedService.deleteReviewComment(request));

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(EXISTING_COMMENT_ID).orElseThrow();
        assertAll(
                () -> assertThat(result.isDeleted()).isFalse(),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(ORIGINAL_UPDATED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project_and_review_comment.sql")
    @Test
    void 같은_updatedAt이면_삭제하지_않는다() {
        // given
        Instant sameUpdatedAt = Instant.parse("2024-01-15T01:00:00Z"); // KST 10:00 (기존과 동일)

        ReviewCommentDeletedRequest request = new ReviewCommentDeletedRequest(
                null,
                EXISTING_COMMENT_ID,
                sameUpdatedAt
        );

        // when
        processingSourceContext.withInboxProcessing(() -> reviewCommentDeletedService.deleteReviewComment(request));

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(EXISTING_COMMENT_ID).orElseThrow();
        assertAll(
                () -> assertThat(result.isDeleted()).isFalse(),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(ORIGINAL_UPDATED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_댓글이면_예외가_발생한다() {
        // given
        Long nonExistentCommentId = 999L;
        ReviewCommentDeletedRequest request = new ReviewCommentDeletedRequest(
                null,
                nonExistentCommentId,
                FIXED_UPDATED_AT
        );

        // when & then
        assertThatThrownBy(() -> processingSourceContext.withInboxProcessing(() -> reviewCommentDeletedService.deleteReviewComment(request)))
                .isInstanceOf(ReviewCommentNotFoundException.class);
    }
}
