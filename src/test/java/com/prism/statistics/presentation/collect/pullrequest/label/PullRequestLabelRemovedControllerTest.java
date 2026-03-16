package com.prism.statistics.presentation.collect.pullrequest.label;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelRemovedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.domain.analysis.metadata.pullrequest.exception.PullRequestNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestLabelRemovedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private PullRequestLabelRemovedService pullRequestLabelRemovedService;

    @Test
    void Label_removed_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "pullRequestNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        willDoNothing().given(pullRequestLabelRemovedService).removePullRequestLabel(any(PullRequestLabelRemovedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/label/removed")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        verify(pullRequestLabelRemovedService).removePullRequestLabel(any(PullRequestLabelRemovedRequest.class));
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "pullRequestNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/label/removed")
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
                    "pullRequestNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(pullRequestLabelRemovedService).removePullRequestLabel(any(PullRequestLabelRemovedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/label/removed")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P01"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }

    @Test
    void 존재하지_않는_PullRequest면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "pullRequestNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new PullRequestNotFoundException())
                .given(pullRequestLabelRemovedService).removePullRequestLabel(any(PullRequestLabelRemovedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/label/removed")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PR00"))
                .andExpect(jsonPath("$.message").value("PullRequest를 찾을 수 없습니다."));
    }
}
