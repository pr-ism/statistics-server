package com.prism.statistics.application.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedCommitDensityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedFileChangeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestMetricsServiceTest {

    @Autowired
    private PullRequestMetricsService metricsService;

    @Autowired
    private JpaPullRequestOpenedChangeSummaryRepository changeSummaryRepository;

    @Autowired
    private JpaPullRequestOpenedCommitDensityRepository commitDensityRepository;

    @Autowired
    private JpaPullRequestOpenedFileChangeRepository fileChangeRepository;

    @Test
    void pull_request_오픈_이벤트를_처리하면_세_종류_파생_지표가_저장된다() {
        // given
        PullRequestOpenCreatedEvent event = createPullRequestOpenCreatedEvent();

        // when
        metricsService.deriveMetrics(event);

        // then
        List<PullRequestOpenedChangeSummary> changeSummaries = changeSummaryRepository.findAll();
        List<PullRequestOpenedCommitDensity> commitDensities = commitDensityRepository.findAll();
        List<PullRequestOpenedFileChange> fileChanges = fileChangeRepository.findAll();

        assertAll(
                () -> assertThat(changeSummaries)
                        .singleElement()
                        .satisfies(summary -> assertAll(
                                () -> assertThat(summary.getPullRequestId()).isEqualTo(1L),
                                () -> assertThat(summary.getTotalChanges()).isEqualTo(16),
                                () -> assertThat(summary.getAvgChangesPerFile()).isEqualTo(new BigDecimal("8.0000"))
                        )),
                () -> assertThat(commitDensities)
                        .singleElement()
                        .satisfies(density -> assertAll(
                                () -> assertThat(density.getCommitDensityPerFile()).isEqualTo(new BigDecimal("2.0000")),
                                () -> assertThat(density.getCommitDensityPerChange()).isEqualTo(new BigDecimal("0.250000"))
                        )),
                () -> assertThat(fileChanges)
                        .hasSize(2)
                        .anySatisfy(item -> assertAll(
                                () -> assertThat(item.getPullRequestId()).isEqualTo(1L),
                                () -> assertThat(item.getChangeType()).isEqualTo(FileChangeType.MODIFIED),
                                () -> assertThat(item.getCount()).isEqualTo(1),
                                () -> assertThat(item.getRatio()).isEqualTo(new BigDecimal("0.50"))
                        )),
                () -> assertThat(fileChanges)
                        .hasSize(2)
                        .anySatisfy(item -> assertAll(
                                () -> assertThat(item.getPullRequestId()).isEqualTo(1L),
                                () -> assertThat(item.getChangeType()).isEqualTo(FileChangeType.ADDED),
                                () -> assertThat(item.getCount()).isEqualTo(1),
                                () -> assertThat(item.getRatio()).isEqualTo(new BigDecimal("0.50"))
                        ))
        );
    }

    private PullRequestOpenCreatedEvent createPullRequestOpenCreatedEvent() {
        PullRequestChangeStats changeStats = PullRequestChangeStats.create(2, 10, 6);
        List<FileData> files = List.of(
                new FileData("src/main/java/Example.java", "modified", 8, 2),
                new FileData("src/main/java/NewFile.java", "added", 2, 4)
        );

        return new PullRequestOpenCreatedEvent(
                1L,
                10L,
                PullRequestState.OPEN,
                changeStats,
                4,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                files,
                List.of()
        );
    }
}
