package com.prism.statistics.presentation.collect.review;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.review.ReviewSubmittedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class ReviewSubmittedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectApiKeyService projectApiKeyService;

    @Autowired
    private ReviewSubmittedService reviewSubmittedService;

    @Test
    void Review_submitted_이벤트_수집_성공_테스트() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
                    "githubPullRequestId": 123456789,
                    "pullRequestNumber": 42,
                    "githubReviewId": 987654321,
                    "reviewer": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "state": "approved",
                    "commitSha": "abc123sha",
                    "body": "LGTM",
                    "commentCount": 3,
                    "submittedAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        post("/collect/review/submitted")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        Review_submitted_이벤트_수집_문서화(resultActions);
    }

    private void Review_submitted_이벤트_수집_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("X-API-Key").description("프로젝트 API Key")
                        ),
                        requestFields(
                                fieldWithPath("runId").description("GitHub Actions Run ID"),
                                fieldWithPath("githubPullRequestId").description("GitHub PullRequest ID").optional(),
                                fieldWithPath("pullRequestNumber").description("PullRequest 번호"),
                                fieldWithPath("githubReviewId").description("GitHub Review ID").optional(),
                                fieldWithPath("reviewer").description("리뷰어 정보"),
                                fieldWithPath("reviewer.login").description("리뷰어 GitHub 로그인"),
                                fieldWithPath("reviewer.id").description("리뷰어 GitHub ID").optional(),
                                fieldWithPath("state").description("리뷰 상태 (approved, changes_requested, commented 등)"),
                                fieldWithPath("commitSha").description("리뷰 대상 커밋 SHA").optional(),
                                fieldWithPath("body").description("리뷰 본문").optional(),
                                fieldWithPath("commentCount").description("코멘트 수"),
                                fieldWithPath("submittedAt").description("리뷰 제출 일시")
                        )
                )
        );
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubPullRequestId": 123456789,
                    "pullRequestNumber": 42,
                    "githubReviewId": 987654321,
                    "reviewer": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "state": "approved",
                    "commitSha": "abc123sha",
                    "body": "LGTM",
                    "commentCount": 3,
                    "submittedAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/collect/review/submitted")
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
                    "githubPullRequestId": 123456789,
                    "pullRequestNumber": 42,
                    "githubReviewId": 987654321,
                    "reviewer": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "state": "approved",
                    "commitSha": "abc123sha",
                    "body": "LGTM",
                    "commentCount": 3,
                    "submittedAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(projectApiKeyService).validateApiKey(TEST_API_KEY);

        // when & then
        mockMvc.perform(
                        post("/collect/review/submitted")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P01"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }
}
