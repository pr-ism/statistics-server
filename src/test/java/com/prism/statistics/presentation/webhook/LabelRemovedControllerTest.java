package com.prism.statistics.presentation.webhook;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.webhook.LabelRemovedService;
import com.prism.statistics.application.webhook.dto.request.LabelRemovedRequest;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class LabelRemovedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private LabelRemovedService labelRemovedService;

    @Test
    void Label_removed_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "prNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        willDoNothing().given(labelRemovedService).removeLabel(eq(TEST_API_KEY), any(LabelRemovedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/label/removed")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());

        verify(labelRemovedService).removeLabel(eq(TEST_API_KEY), any(LabelRemovedRequest.class));
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "prNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                post("/webhook/label/removed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_프로젝트면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "prNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new ProjectNotFoundException())
                .given(labelRemovedService).removeLabel(eq(TEST_API_KEY), any(LabelRemovedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/label/removed")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("P00"))
        .andExpect(jsonPath("$.message").value("프로젝트를 찾을 수 없습니다."));
    }

    @Test
    void 존재하지_않는_PR이면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "prNumber": 42,
                    "label": {
                        "name": "bug"
                    },
                    "unlabeledAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new PullRequestNotFoundException())
                .given(labelRemovedService).removeLabel(eq(TEST_API_KEY), any(LabelRemovedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/label/removed")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("PR00"))
        .andExpect(jsonPath("$.message").value("PR을 찾을 수 없습니다."));
    }
}
