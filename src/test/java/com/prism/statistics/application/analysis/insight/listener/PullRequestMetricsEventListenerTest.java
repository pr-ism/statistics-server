package com.prism.statistics.application.analysis.insight.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedCommitDensityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedFileChangeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestMetricsEventListenerTest {

    @Autowired
    private PullRequestMetricsEventListener eventListener;

    @Autowired
    private JpaPullRequestOpenedChangeSummaryRepository changeSummaryRepository;

    @Autowired
    private JpaPullRequestOpenedCommitDensityRepository commitDensityRepository;

    @Autowired
    private JpaPullRequestOpenedFileChangeRepository fileChangeRepository;

    @Test
    void pull_request_오픈_이벤트를_처리하면_파생_지표가_저장된다() {
        // given
        PullRequestOpenCreatedEvent event = createPullRequestOpenCreatedEvent();

        // when
        eventListener.handle(event);

        // then
        assertAll(
                () -> assertThat(changeSummaryRepository.count()).isEqualTo(1),
                () -> assertThat(commitDensityRepository.count()).isEqualTo(1),
                () -> assertThat(fileChangeRepository.count()).isEqualTo(1)
        );
    }

    private PullRequestOpenCreatedEvent createPullRequestOpenCreatedEvent() {
        PullRequestChangeStats changeStats = PullRequestChangeStats.create(1, 5, 5);
        List<FileData> files = List.of(
                new FileData("src/main/java/Example.java", "modified", 5, 5)
        );

        return new PullRequestOpenCreatedEvent(
                1L,
                10L,
                PullRequestState.OPEN,
                changeStats,
                2,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                files,
                List.of()
        );
    }
}
