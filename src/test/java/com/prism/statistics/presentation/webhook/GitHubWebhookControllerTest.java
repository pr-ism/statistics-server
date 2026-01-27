package com.prism.statistics.presentation.webhook;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.webhook.PrOpenedHandler;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class GitHubWebhookControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private PrOpenedHandler prOpenedHandler;

    @Test
    void PR_opened_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "eventType": "pull_request",
                    "action": "opened",
                    "repositoryFullName": "owner/repo",
                    "isDraft": false,
                    "pullRequest": {
                        "number": 42,
                        "title": "feat: 새로운 기능 추가",
                        "url": "https://github.com/owner/repo/pull/42",
                        "additions": 100,
                        "deletions": 50,
                        "changedFiles": 10,
                        "createdAt": "2024-01-15T10:00:00Z",
                        "author": { "login": "test-author" },
                        "commits": {
                            "totalCount": 2,
                            "nodes": [
                                { "commit": { "oid": "abc123", "committedDate": "2024-01-15T09:00:00Z" } },
                                { "commit": { "oid": "def456", "committedDate": "2024-01-15T09:30:00Z" } }
                            ]
                        }
                    },
                    "files": [
                        { "filename": "src/main/java/Example.java", "status": "modified", "additions": 80, "deletions": 30 },
                        { "filename": "src/main/java/NewFile.java", "status": "added", "additions": 20, "deletions": 0 }
                    ]
                }
                """;

        willDoNothing().given(prOpenedHandler).handle(anyString(), any(PrOpenedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/pr/opened")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());

        verify(prOpenedHandler).handle(anyString(), any(PrOpenedRequest.class));
    }

    @Test
    void Draft_PR_웹훅_요청도_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "eventType": "pull_request",
                    "action": "opened",
                    "repositoryFullName": "owner/repo",
                    "isDraft": true,
                    "pullRequest": {
                        "number": 43,
                        "title": "draft: 작업 중인 PR",
                        "url": "https://github.com/owner/repo/pull/43",
                        "additions": 50,
                        "deletions": 10,
                        "changedFiles": 3,
                        "createdAt": "2024-01-15T11:00:00Z",
                        "author": { "login": "test-author" },
                        "commits": {
                            "totalCount": 1,
                            "nodes": [
                                { "commit": { "oid": "ghi789", "committedDate": "2024-01-15T10:30:00Z" } }
                            ]
                        }
                    },
                    "files": [
                        { "filename": "src/main/java/Draft.java", "status": "added", "additions": 50, "deletions": 10 }
                    ]
                }
                """;

        willDoNothing().given(prOpenedHandler).handle(anyString(), any(PrOpenedRequest.class));

        // when & then
        mockMvc.perform(
                post("/webhook/pr/opened")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());

        verify(prOpenedHandler).handle(anyString(), any(PrOpenedRequest.class));
    }
}
