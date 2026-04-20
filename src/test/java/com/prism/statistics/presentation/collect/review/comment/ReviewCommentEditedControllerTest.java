package com.prism.statistics.presentation.collect.review.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentEditedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewCommentNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class ReviewCommentEditedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectApiKeyService projectApiKeyService;

    @Autowired
    private ReviewCommentEditedService reviewCommentEditedService;

    @Test
    void Review_comment_edited_이벤트_수집_성공_테스트() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        post("/collect/review/comment/edited")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        then(reviewCommentEditedService).should()
                .editReviewComment(any(ReviewCommentEditedRequest.class));

        Review_comment_edited_이벤트_수집_문서화(resultActions);
    }

    private void Review_comment_edited_이벤트_수집_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("X-API-Key").description("프로젝트 API Key")
                        ),
                        requestFields(
                                fieldWithPath("runId").description("GitHub Actions Run ID"),
                                fieldWithPath("githubCommentId").description("GitHub Comment ID").optional(),
                                fieldWithPath("body").description("수정된 코멘트 내용").optional(),
                                fieldWithPath("updatedAt").description("수정 일시")
                        )
                )
        );
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/collect/review/comment/edited")
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
                    "runId": 12345,
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(projectApiKeyService).validateApiKey(TEST_API_KEY);

        // when & then
        mockMvc.perform(
                        post("/collect/review/comment/edited")
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
                    "runId": 12345,
                    "githubCommentId": 123456789,
                    "body": "수정된 댓글 내용입니다.",
                    "updatedAt": "2024-01-15T11:00:00Z"
                }
                """;

        willThrow(new ReviewCommentNotFoundException())
                .given(reviewCommentEditedService).editReviewComment(any(ReviewCommentEditedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/review/comment/edited")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("RC00"))
                .andExpect(jsonPath("$.message").value("리뷰 댓글을 찾을 수 없습니다."));
    }
}
