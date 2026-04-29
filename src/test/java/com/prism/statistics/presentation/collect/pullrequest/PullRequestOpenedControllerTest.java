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
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestOpenedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectIdResolvingFacade projectIdResolvingFacade;

    @Test
    void PullRequest_opened_이벤트_수집_성공_테스트() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
                    "isDraft": false,
                    "pullRequest": {
                        "githubPullRequestId": 100,
                        "number": 42,
                        "title": "feat: 새로운 기능 추가",
                        "url": "https://github.com/owner/repo/pull/42",
                        "headCommitSha": "def456",
                        "additions": 100,
                        "deletions": 50,
                        "changedFiles": 10,
                        "createdAt": "2024-01-15T10:00:00Z",
                        "author": { "login": "test-author", "id": 1 },
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

        willDoNothing().given(projectIdResolvingFacade).createPullRequest(eq(TEST_API_KEY), any(PullRequestOpenedRequest.class));

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        post("/collect/pull-request/opened")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        verify(projectIdResolvingFacade).createPullRequest(eq(TEST_API_KEY), any(PullRequestOpenedRequest.class));

        PullRequest_opened_이벤트_수집_문서화(resultActions);
    }

    private void PullRequest_opened_이벤트_수집_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("X-API-Key").description("프로젝트 API Key")
                        ),
                        requestFields(
                                fieldWithPath("runId").description("GitHub Actions Run ID"),
                                fieldWithPath("isDraft").description("Draft PR 여부"),
                                fieldWithPath("pullRequest").description("PullRequest 정보"),
                                fieldWithPath("pullRequest.githubPullRequestId").description("GitHub PullRequest ID").optional(),
                                fieldWithPath("pullRequest.number").description("PullRequest 번호"),
                                fieldWithPath("pullRequest.title").description("PullRequest 제목"),
                                fieldWithPath("pullRequest.url").description("PullRequest URL"),
                                fieldWithPath("pullRequest.headCommitSha").description("Head 커밋 SHA").optional(),
                                fieldWithPath("pullRequest.additions").description("추가된 라인 수"),
                                fieldWithPath("pullRequest.deletions").description("삭제된 라인 수"),
                                fieldWithPath("pullRequest.changedFiles").description("변경된 파일 수"),
                                fieldWithPath("pullRequest.createdAt").description("생성 일시"),
                                fieldWithPath("pullRequest.author").description("작성자 정보"),
                                fieldWithPath("pullRequest.author.login").description("작성자 GitHub 로그인"),
                                fieldWithPath("pullRequest.author.id").description("작성자 GitHub ID").optional(),
                                fieldWithPath("pullRequest.commits").description("커밋 정보"),
                                fieldWithPath("pullRequest.commits.totalCount").description("총 커밋 수"),
                                fieldWithPath("pullRequest.commits.nodes").description("커밋 목록"),
                                fieldWithPath("pullRequest.commits.nodes[].commit").description("커밋 상세 정보"),
                                fieldWithPath("pullRequest.commits.nodes[].commit.oid").description("커밋 SHA"),
                                fieldWithPath("pullRequest.commits.nodes[].commit.committedDate").description("커밋 일시"),
                                fieldWithPath("files").description("변경된 파일 목록"),
                                fieldWithPath("files[].filename").description("파일 경로"),
                                fieldWithPath("files[].status").description("파일 상태 (added, modified, removed 등)"),
                                fieldWithPath("files[].additions").description("추가된 라인 수"),
                                fieldWithPath("files[].deletions").description("삭제된 라인 수")
                        )
                )
        );
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/opened")
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

        willThrow(new InvalidApiKeyException())
                .given(projectIdResolvingFacade).createPullRequest(eq(TEST_API_KEY), any(PullRequestOpenedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/pull-request/opened")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P01"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }
}
