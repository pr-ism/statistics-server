package com.prism.statistics.presentation.collect.pullrequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.collect.ProjectIdResolvingFacade;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestReadyForReviewControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectIdResolvingFacade projectIdResolvingFacade;

    @Test
    void PullRequest_ready_for_review_이벤트_수집_성공_테스트() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
                    "pullRequestNumber": 42,
                    "readyForReviewAt": "2024-01-15T12:00:00Z"
                }
                """;

        willDoNothing().given(projectIdResolvingFacade).readyForReview(eq(TEST_API_KEY), any(PullRequestReadyForReviewRequest.class));

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        post("/collect/pull-request/ready-for-review")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        verify(projectIdResolvingFacade).readyForReview(eq(TEST_API_KEY), any(PullRequestReadyForReviewRequest.class));

        PullRequest_ready_for_review_이벤트_수집_문서화(resultActions);
    }

    private void PullRequest_ready_for_review_이벤트_수집_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("X-API-Key").description("프로젝트 API Key")
                        ),
                        requestFields(
                                fieldWithPath("runId").description("GitHub Actions Run ID"),
                                fieldWithPath("pullRequestNumber").description("PullRequest 번호"),
                                fieldWithPath("readyForReviewAt").description("리뷰 준비 상태가 된 일시")
                        )
                )
        );
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/ready-for-review")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 유효하지_않은_API_Key면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "pullRequestNumber": 42,
                    "readyForReviewAt": "2024-01-15T12:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(projectIdResolvingFacade).readyForReview(eq(TEST_API_KEY), any(PullRequestReadyForReviewRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/ready-for-review")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P01"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }
}
