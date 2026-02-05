package com.prism.statistics.application.analysis.metadata.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest.CommentAuthorData;
import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import com.prism.statistics.domain.analysis.metadata.review.enums.CommentSide;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewCommentRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewCommentCreatedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final Instant TEST_CREATED_AT = Instant.parse("2024-01-15T10:00:00Z");
    private static final LocalDateTime EXPECTED_CREATED_AT = LocalDateTime.of(2024, 1, 15, 19, 0, 0);

    @Autowired
    private ReviewCommentCreatedService reviewCommentCreatedService;

    @Autowired
    private JpaReviewCommentRepository jpaReviewCommentRepository;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 리뷰_댓글_생성_시_ReviewComment가_저장된다() {
        // given
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(100L);

        // when
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);

        // then
        assertThat(jpaReviewCommentRepository.count()).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 리뷰_댓글_생성_시_ReviewComment_정보가_올바르게_저장된다() {
        // given
        Long githubCommentId = 100L;
        Long githubReviewId = 200L;
        String authorLogin = "reviewer1";
        Long authorId = 12345L;
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(
                githubCommentId, githubReviewId,
                "코드 리뷰 댓글입니다.",
                "src/main/java/Example.java",
                10, null,
                "right",
                "abc123sha",
                null,
                authorLogin, authorId
        );

        // when
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);

        // then
        List<ReviewComment> comments = jpaReviewCommentRepository.findAll();
        assertThat(comments).hasSize(1);

        ReviewComment comment = comments.get(0);
        assertAll(
                () -> assertThat(comment.getGithubCommentId()).isEqualTo(githubCommentId),
                () -> assertThat(comment.getGithubReviewId()).isEqualTo(githubReviewId),
                () -> assertThat(comment.getBody()).isEqualTo("코드 리뷰 댓글입니다."),
                () -> assertThat(comment.getPath()).isEqualTo("src/main/java/Example.java"),
                () -> assertThat(comment.getLineRange()).isNotNull(),
                () -> assertThat(comment.getLineRange().getEndLine()).isEqualTo(10),
                () -> assertThat(comment.getLineRange().getStartLine()).isNull(),
                () -> assertThat(comment.getSide()).isEqualTo(CommentSide.RIGHT),
                () -> assertThat(comment.getCommitSha()).isEqualTo("abc123sha"),
                () -> assertThat(comment.getParentCommentId().hasParent()).isFalse(),
                () -> assertThat(comment.getAuthorMention()).isEqualTo(authorLogin),
                () -> assertThat(comment.getAuthorGithubUid()).isEqualTo(authorId),
                () -> assertThat(comment.getGithubCreatedAt()).isEqualTo(EXPECTED_CREATED_AT),
                () -> assertThat(comment.getGithubUpdatedAt()).isEqualTo(EXPECTED_CREATED_AT),
                () -> assertThat(comment.isDeleted()).isFalse()
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 멀티라인_댓글을_저장한다() {
        // given
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(
                100L, 200L,
                "멀티라인 댓글입니다.",
                "src/main/java/Example.java",
                20, 10,
                "right",
                "abc123sha",
                null,
                "reviewer1", 12345L
        );

        // when
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);

        // then
        ReviewComment comment = jpaReviewCommentRepository.findAll().get(0);
        assertAll(
                () -> assertThat(comment.getLineRange().getStartLine()).isEqualTo(10),
                () -> assertThat(comment.getLineRange().getEndLine()).isEqualTo(20),
                () -> assertThat(comment.getLineRange().isSingleLine()).isFalse()
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 답글_댓글을_저장한다() {
        // given
        Long parentCommentId = 50L;
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(
                100L, 200L,
                "답글입니다.",
                "src/main/java/Example.java",
                10, null,
                "right",
                "abc123sha",
                parentCommentId,
                "reviewer1", 12345L
        );

        // when
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);

        // then
        ReviewComment comment = jpaReviewCommentRepository.findAll().get(0);
        assertAll(
                () -> assertThat(comment.getParentCommentId().hasParent()).isTrue(),
                () -> assertThat(comment.getParentCommentId().getValue()).isEqualTo(parentCommentId)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void LEFT_side_댓글을_저장한다() {
        // given
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(
                100L, 200L,
                "왼쪽 댓글입니다.",
                "src/main/java/Example.java",
                10, null,
                "left",
                "abc123sha",
                null,
                "reviewer1", 12345L
        );

        // when
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);

        // then
        ReviewComment comment = jpaReviewCommentRepository.findAll().get(0);
        assertThat(comment.getSide()).isEqualTo(CommentSide.LEFT);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 중복_댓글_생성_시_저장되지_않는다() {
        // given
        Long sameGithubCommentId = 100L;
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(sameGithubCommentId);

        // when
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);
        reviewCommentCreatedService.createReviewComment(TEST_API_KEY, request);

        // then
        assertThat(jpaReviewCommentRepository.count()).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        ReviewCommentCreatedRequest request = createReviewCommentCreatedRequest(100L);
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> reviewCommentCreatedService.createReviewComment(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    private ReviewCommentCreatedRequest createReviewCommentCreatedRequest(Long githubCommentId) {
        return createReviewCommentCreatedRequest(
                githubCommentId, 200L,
                "코드 리뷰 댓글입니다.",
                "src/main/java/Example.java",
                10, null,
                "right",
                "abc123sha",
                null,
                "reviewer1", 12345L
        );
    }

    private ReviewCommentCreatedRequest createReviewCommentCreatedRequest(
            Long githubCommentId,
            Long githubReviewId,
            String body,
            String path,
            int line,
            Integer startLine,
            String side,
            String commitSha,
            Long inReplyToId,
            String authorLogin,
            Long authorId
    ) {
        return new ReviewCommentCreatedRequest(
                githubCommentId,
                githubReviewId,
                body,
                path,
                line,
                startLine,
                side,
                commitSha,
                inReplyToId,
                new CommentAuthorData(authorLogin, authorId),
                TEST_CREATED_AT,
                TEST_CREATED_AT
        );
    }
}
