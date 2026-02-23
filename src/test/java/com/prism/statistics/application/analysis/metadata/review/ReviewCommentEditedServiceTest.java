package com.prism.statistics.application.analysis.metadata.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewCommentRepository;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewCommentNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewCommentEditedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final Long EXISTING_COMMENT_ID = 100L;
    private static final String ORIGINAL_BODY = "원본 댓글 내용";
    private static final LocalDateTime ORIGINAL_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Autowired
    private ReviewCommentEditedService reviewCommentEditedService;

    @Autowired
    private JpaReviewCommentRepository jpaReviewCommentRepository;

    @Sql("/sql/webhook/insert_project_and_review_comment.sql")
    @Test
    void 최신_updatedAt이면_body를_업데이트한다() {
        // given
        String newBody = "수정된 댓글 내용";
        Instant newerUpdatedAt = Instant.parse("2024-01-15T02:00:00Z"); // KST 11:00
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2024, 1, 15, 11, 0, 0);

        ReviewCommentEditedRequest request = new ReviewCommentEditedRequest(
                EXISTING_COMMENT_ID,
                newBody,
                newerUpdatedAt
        );

        // when
        reviewCommentEditedService.editReviewComment(TEST_API_KEY, request);

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(EXISTING_COMMENT_ID).orElseThrow();
        assertAll(
                () -> assertThat(result.getBody()).isEqualTo(newBody),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(expectedUpdatedAt)
        );
    }

    @Sql("/sql/webhook/insert_project_and_review_comment.sql")
    @Test
    void 과거_updatedAt이면_업데이트하지_않는다() {
        // given
        String newBody = "수정된 댓글 내용";
        Instant olderUpdatedAt = Instant.parse("2024-01-15T00:00:00Z"); // KST 09:00 (기존 10:00보다 과거)

        ReviewCommentEditedRequest request = new ReviewCommentEditedRequest(
                EXISTING_COMMENT_ID,
                newBody,
                olderUpdatedAt
        );

        // when
        reviewCommentEditedService.editReviewComment(TEST_API_KEY, request);

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(EXISTING_COMMENT_ID).orElseThrow();
        assertAll(
                () -> assertThat(result.getBody()).isEqualTo(ORIGINAL_BODY),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(ORIGINAL_UPDATED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project_and_review_comment.sql")
    @Test
    void 같은_updatedAt이면_업데이트하지_않는다() {
        // given
        String newBody = "수정된 댓글 내용";
        Instant sameUpdatedAt = Instant.parse("2024-01-15T01:00:00Z"); // KST 10:00 (기존과 동일)

        ReviewCommentEditedRequest request = new ReviewCommentEditedRequest(
                EXISTING_COMMENT_ID,
                newBody,
                sameUpdatedAt
        );

        // when
        reviewCommentEditedService.editReviewComment(TEST_API_KEY, request);

        // then
        ReviewComment result = jpaReviewCommentRepository.findByGithubCommentId(EXISTING_COMMENT_ID).orElseThrow();
        assertAll(
                () -> assertThat(result.getBody()).isEqualTo(ORIGINAL_BODY),
                () -> assertThat(result.getGithubUpdatedAt()).isEqualTo(ORIGINAL_UPDATED_AT)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        String invalidApiKey = "invalid-api-key";
        ReviewCommentEditedRequest request = new ReviewCommentEditedRequest(
                EXISTING_COMMENT_ID,
                "수정된 댓글 내용",
                Instant.now()
        );

        // when & then
        assertThatThrownBy(() -> reviewCommentEditedService.editReviewComment(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 존재하지_않는_댓글이면_예외가_발생한다() {
        // given
        Long nonExistentCommentId = 999L;
        ReviewCommentEditedRequest request = new ReviewCommentEditedRequest(
                nonExistentCommentId,
                "수정된 댓글 내용",
                Instant.now()
        );

        // when & then
        assertThatThrownBy(() -> reviewCommentEditedService.editReviewComment(TEST_API_KEY, request))
                .isInstanceOf(ReviewCommentNotFoundException.class);
    }
}
