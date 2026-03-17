package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestClosedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
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
class PullRequestClosedInboxHandlerTest {

    @Mock
    PullRequestClosedService pullRequestClosedService;

    PullRequestClosedInboxHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new PullRequestClosedInboxHandler(objectMapper, pullRequestClosedService);
    }

    @Test
    void supportType은_PULL_REQUEST_CLOSED를_반환한다() {
        assertThat(handler.supportType()).isEqualTo(CollectInboxType.PULL_REQUEST_CLOSED);
    }

    @Test
    void payloadJson을_역직렬화하여_서비스를_호출한다() {
        // given
        String payloadJson = """
                {
                    "runId": 100,
                    "pullRequestNumber": 10,
                    "isMerged": true,
                    "closedAt": "2026-03-17T10:00:00Z",
                    "mergedAt": "2026-03-17T10:00:00Z"
                }
                """;
        CollectInboxContext context = new CollectInboxContext(1L, payloadJson);

        // when
        handler.handle(context);

        // then
        ArgumentCaptor<PullRequestClosedRequest> captor = ArgumentCaptor.forClass(PullRequestClosedRequest.class);
        verify(pullRequestClosedService).closePullRequest(eq(1L), captor.capture());
        assertThat(captor.getValue().pullRequestNumber()).isEqualTo(10);
    }

    @Test
    void 역직렬화_실패시_RuntimeException으로_감싸서_던진다() {
        // given
        CollectInboxContext context = new CollectInboxContext(1L, "{invalid-json}");

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isInstanceOf(RuntimeException.class);
    }
}
