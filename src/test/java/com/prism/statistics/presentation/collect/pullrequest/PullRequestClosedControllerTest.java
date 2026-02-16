package com.prism.statistics.presentation.collect.pullrequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestClosedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestClosedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private PullRequestClosedService pullRequestClosedService;

    @Test
    void Pull_Request_closed_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "pullRequestNumber": 42,
                    "headCommitSha": "abc123",
                    "isMerged": false,
                    "closedAt": "2024-01-15T12:00:00Z",
                    "mergedAt": null
                }
                """;

        willDoNothing().given(pullRequestClosedService).closePullRequest(eq(TEST_API_KEY), any(PullRequestClosedRequest.class));

        // when & then
        mockMvc.perform(
                post("/collect/pull-request/closed")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());

        verify(pullRequestClosedService).closePullRequest(eq(TEST_API_KEY), any(PullRequestClosedRequest.class));
    }

    @Test
    void Pull_Request_merged_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "pullRequestNumber": 42,
                    "headCommitSha": "abc123",
                    "isMerged": true,
                    "closedAt": "2024-01-15T12:00:00Z",
                    "mergedAt": "2024-01-15T12:00:00Z"
                }
                """;

        willDoNothing().given(pullRequestClosedService).closePullRequest(eq(TEST_API_KEY), any(PullRequestClosedRequest.class));

        // when & then
        mockMvc.perform(
                post("/collect/pull-request/closed")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());

        verify(pullRequestClosedService).closePullRequest(eq(TEST_API_KEY), any(PullRequestClosedRequest.class));
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                post("/collect/pull-request/closed")
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
                    "headCommitSha": "abc123",
                    "isMerged": false,
                    "closedAt": "2024-01-15T12:00:00Z",
                    "mergedAt": null
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(pullRequestClosedService).closePullRequest(eq(TEST_API_KEY), any(PullRequestClosedRequest.class));

        // when & then
        mockMvc.perform(
                post("/collect/pull-request/closed")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("P01"))
        .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }
}
