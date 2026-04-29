package com.prism.statistics.presentation.collect.pullrequest;

import static org.mockito.ArgumentMatchers.any;
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

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestSynchronizedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestSynchronizedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectApiKeyService projectApiKeyService;

    @Autowired
    private PullRequestSynchronizedService pullRequestSynchronizedService;

    @Test
    void PullRequest_synchronized_이벤트_수집_성공_테스트() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
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
                        {"filename": "src/main/java/Example.java", "status": "modified", "additions": 100, "deletions": 50, "previousFilename": null},
                        {"filename": "src/main/java/NewFile.java", "status": "added", "additions": 100, "deletions": 30, "previousFilename": null}
                    ]
                }
                """;

        willDoNothing().given(pullRequestSynchronizedService).synchronizePullRequest(any(PullRequestSynchronizedRequest.class));

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        post("/collect/pull-request/synchronized")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        verify(pullRequestSynchronizedService).synchronizePullRequest(any(PullRequestSynchronizedRequest.class));

        PullRequest_synchronized_이벤트_수집_문서화(resultActions);
    }

    private void PullRequest_synchronized_이벤트_수집_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("X-API-Key").description("프로젝트 API Key")
                        ),
                        requestFields(
                                fieldWithPath("runId").description("GitHub Actions Run ID"),
                                fieldWithPath("githubPullRequestId").description("GitHub PullRequest ID"),
                                fieldWithPath("pullRequestNumber").description("PullRequest 번호"),
                                fieldWithPath("headCommitSha").description("Head 커밋 SHA"),
                                fieldWithPath("additions").description("추가된 라인 수"),
                                fieldWithPath("deletions").description("삭제된 라인 수"),
                                fieldWithPath("changedFiles").description("변경된 파일 수"),
                                fieldWithPath("commits").description("커밋 정보"),
                                fieldWithPath("commits.totalCount").description("총 커밋 수"),
                                fieldWithPath("commits.nodes").description("커밋 목록"),
                                fieldWithPath("commits.nodes[].sha").description("커밋 SHA"),
                                fieldWithPath("commits.nodes[].committedDate").description("커밋 일시"),
                                fieldWithPath("files").description("변경된 파일 목록"),
                                fieldWithPath("files[].filename").description("파일 경로"),
                                fieldWithPath("files[].status").description("파일 상태 (added, modified, removed 등)"),
                                fieldWithPath("files[].additions").description("추가된 라인 수"),
                                fieldWithPath("files[].deletions").description("삭제된 라인 수"),
                                fieldWithPath("files[].previousFilename").description("이전 파일명 (rename 시)").optional()
                        )
                )
        );
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
                .given(projectApiKeyService).validateApiKey(TEST_API_KEY);

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
