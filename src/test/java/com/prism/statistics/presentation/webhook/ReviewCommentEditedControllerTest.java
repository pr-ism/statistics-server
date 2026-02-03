package com.prism.statistics.presentation.webhook;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.webhook.ReviewCommentEditedService;
import com.prism.statistics.application.webhook.dto.request.ReviewCommentEditedRequest;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.reviewcomment.persistence.exception.ReviewCommentNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class ReviewCommentEditedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ReviewCommentEditedService reviewCommentEditedService;

    @Test
    void Review_comment_edited_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                post("/webhook/review-comment/edited")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());

        then(reviewCommentEditedService).should()
                .editReviewComment(eq(TEST_API_KEY), any(ReviewCommentEditedRequest.class));
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                post("/webhook/review-comment/edited")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void 유효하지_않은_API_Key면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(reviewCommentEditedService).editReviewComment(eq(TEST_API_KEY), any(ReviewCommentEditedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/review-comment/edited")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("P01"))
        .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }

    @Test
    void ReviewComment를_찾을_수_없으면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        willThrow(new ReviewCommentNotFoundException())
                .given(reviewCommentEditedService).editReviewComment(eq(TEST_API_KEY), any(ReviewCommentEditedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/review-comment/edited")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("RC00"))
        .andExpect(jsonPath("$.message").value("리뷰 댓글을 찾을 수 없습니다."));
    }
}
