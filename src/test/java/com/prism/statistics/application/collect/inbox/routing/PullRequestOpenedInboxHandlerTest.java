package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestOpenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestOpenedInboxHandlerTest {

    @Mock
    PullRequestOpenedService pullRequestOpenedService;

    PullRequestOpenedInboxHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CollectInboxPayloadDeserializer deserializer = new CollectInboxPayloadDeserializer(objectMapper);
        handler = new PullRequestOpenedInboxHandler(deserializer, pullRequestOpenedService);
    }

    @Test
    void supportType은_PULL_REQUEST_OPENED를_반환한다() {
        assertThat(handler.supportType()).isEqualTo(CollectInboxType.PULL_REQUEST_OPENED);
    }

    @Test
    void payloadJson을_역직렬화하여_서비스를_호출한다() {
        // given
        String payloadJson = """
                {
                    "runId": 100,
                    "isDraft": false,
                    "pullRequest": {
                        "githubPullRequestId": 1,
                        "number": 10,
                        "title": "제목",
                        "url": "https://github.com/org/repo/pull/10",
                        "headCommitSha": "abc123",
                        "additions": 5,
                        "deletions": 3,
                        "changedFiles": 2,
                        "createdAt": "2026-03-17T10:00:00Z",
                        "author": { "login": "user1", "id": 1 },
                        "commits": {
                            "totalCount": 1,
                            "nodes": [{ "commit": { "oid": "abc123", "committedDate": "2026-03-17T10:00:00Z" } }]
                        }
                    },
                    "files": [{ "filename": "Main.java", "status": "added", "additions": 5, "deletions": 0 }]
                }
                """;
        CollectInboxContext context = new CollectInboxContext(1L, payloadJson);

        // when
        handler.handle(context);

        // then
        ArgumentCaptor<PullRequestOpenedRequest> captor = ArgumentCaptor.forClass(PullRequestOpenedRequest.class);
        verify(pullRequestOpenedService).createPullRequest(eq(1L), captor.capture());
        assertThat(captor.getValue().runId()).isEqualTo(100L);
        assertThat(captor.getValue().pullRequest().number()).isEqualTo(10);
    }

    @Test
    void 역직렬화_실패시_IllegalArgumentException을_던진다() {
        // given
        CollectInboxContext context = new CollectInboxContext(1L, "{invalid-json}");

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 서비스_호출_실패시_예외가_원형_그대로_전파된다() {
        // given
        String payloadJson = """
                {
                    "runId": 100,
                    "isDraft": false,
                    "pullRequest": {
                        "githubPullRequestId": 1,
                        "number": 10,
                        "title": "제목",
                        "url": "https://github.com/org/repo/pull/10",
                        "headCommitSha": "abc123",
                        "additions": 0,
                        "deletions": 0,
                        "changedFiles": 0,
                        "createdAt": "2026-03-17T10:00:00Z",
                        "author": { "login": "user1", "id": 1 },
                        "commits": { "totalCount": 0, "nodes": [] }
                    },
                    "files": []
                }
                """;
        CollectInboxContext context = new CollectInboxContext(1L, payloadJson);
        IllegalArgumentException businessException = new IllegalArgumentException("비즈니스 오류");
        willThrow(businessException)
                .given(pullRequestOpenedService).createPullRequest(any(), any());

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isSameAs(businessException);
    }
}
