package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestSynchronizedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
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
class PullRequestSynchronizedInboxHandlerTest {

    @Mock
    PullRequestSynchronizedService pullRequestSynchronizedService;

    PullRequestSynchronizedInboxHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new PullRequestSynchronizedInboxHandler(objectMapper, pullRequestSynchronizedService);
    }

    @Test
    void supportType은_PULL_REQUEST_SYNCHRONIZED를_반환한다() {
        assertThat(handler.supportType()).isEqualTo(CollectInboxType.PULL_REQUEST_SYNCHRONIZED);
    }

    @Test
    void payloadJson을_역직렬화하여_서비스를_호출한다() {
        // given
        String payloadJson = """
                {
                    "runId": 100,
                    "githubPullRequestId": 1,
                    "pullRequestNumber": 10,
                    "headCommitSha": "abc123",
                    "additions": 5,
                    "deletions": 3,
                    "changedFiles": 2,
                    "commits": {
                        "totalCount": 1,
                        "nodes": [{ "sha": "abc123", "committedDate": "2026-03-17T10:00:00Z" }]
                    },
                    "files": [{ "filename": "Main.java", "status": "added", "additions": 5, "deletions": 0, "previousFilename": null }]
                }
                """;
        CollectInboxContext context = new CollectInboxContext(null, payloadJson);

        // when
        handler.handle(context);

        // then
        ArgumentCaptor<PullRequestSynchronizedRequest> captor = ArgumentCaptor.forClass(PullRequestSynchronizedRequest.class);
        verify(pullRequestSynchronizedService).synchronizePullRequest(captor.capture());
        assertThat(captor.getValue().pullRequestNumber()).isEqualTo(10);
    }

    @Test
    void 역직렬화_실패시_RuntimeException으로_감싸서_던진다() {
        // given
        CollectInboxContext context = new CollectInboxContext(null, "{invalid-json}");

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isInstanceOf(RuntimeException.class);
    }
}
