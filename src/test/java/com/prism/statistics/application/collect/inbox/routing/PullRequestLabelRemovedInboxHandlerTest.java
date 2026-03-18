package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelRemovedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
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
class PullRequestLabelRemovedInboxHandlerTest {

    @Mock
    PullRequestLabelRemovedService pullRequestLabelRemovedService;

    PullRequestLabelRemovedInboxHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CollectInboxPayloadDeserializer deserializer = new CollectInboxPayloadDeserializer(objectMapper);
        handler = new PullRequestLabelRemovedInboxHandler(deserializer, pullRequestLabelRemovedService);
    }

    @Test
    void supportType은_PULL_REQUEST_LABEL_REMOVED를_반환한다() {
        assertThat(handler.supportType()).isEqualTo(CollectInboxType.PULL_REQUEST_LABEL_REMOVED);
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
                    "label": { "name": "bug" },
                    "unlabeledAt": "2026-03-17T10:00:00Z"
                }
                """;
        CollectInboxContext context = new CollectInboxContext(null, payloadJson);

        // when
        handler.handle(context);

        // then
        ArgumentCaptor<PullRequestLabelRemovedRequest> captor = ArgumentCaptor.forClass(PullRequestLabelRemovedRequest.class);
        verify(pullRequestLabelRemovedService).removePullRequestLabel(captor.capture());
        assertThat(captor.getValue().runId()).isEqualTo(100L);
        assertThat(captor.getValue().label().name()).isEqualTo("bug");
    }

    @Test
    void 역직렬화_실패시_IllegalArgumentException을_던진다() {
        // given
        CollectInboxContext context = new CollectInboxContext(null, "{invalid-json}");

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
