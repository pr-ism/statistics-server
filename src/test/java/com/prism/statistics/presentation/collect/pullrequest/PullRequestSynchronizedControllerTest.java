package com.prism.statistics.presentation.collect.pullrequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestSynchronizedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestSynchronizedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private PullRequestSynchronizedService pullRequestSynchronizedService;

    @Test
    void Pull_Request_synchronized_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "githubPullRequestId": 100,
                    "pullRequestNumber": 42,
                    "headCommitSha": "sha3",
                    "additions": 200,
                    "deletions": 80,
                    "changedFiles": 15,
                    "commits": {
                        "totalCount": 3,
                        "nodes": [
                            {"sha": "sha1", "committedDate": "2024-01-15T09:00:00Z"},
                            {"sha": "sha2", "committedDate": "2024-01-15T09:30:00Z"},
                            {"sha": "sha3", "committedDate": "2024-01-15T10:00:00Z"}
                        ]
                    },
                    "files": [
                        {"filename": "src/main/java/Example.java", "status": "modified", "additions": 100, "deletions": 50},
                        {"filename": "src/main/java/NewFile.java", "status": "added", "additions": 100, "deletions": 30}
                    ]
                }
                """;

        willDoNothing().given(pullRequestSynchronizedService).synchronizePullRequest(eq(TEST_API_KEY), any(PullRequestSynchronizedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/synchronized")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isOk());

        verify(pullRequestSynchronizedService).synchronizePullRequest(eq(TEST_API_KEY), any(PullRequestSynchronizedRequest.class));
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/synchronized")
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
                    "githubPullRequestId": 100,
                    "pullRequestNumber": 42,
                    "headCommitSha": "sha3",
                    "additions": 200,
                    "deletions": 80,
                    "changedFiles": 15,
                    "commits": {
                        "totalCount": 3,
                        "nodes": [
                            {"sha": "sha3", "committedDate": "2024-01-15T10:00:00Z"}
                        ]
                    },
                    "files": []
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(pullRequestSynchronizedService).synchronizePullRequest(eq(TEST_API_KEY), any(PullRequestSynchronizedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/synchronized")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P01"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }
}
