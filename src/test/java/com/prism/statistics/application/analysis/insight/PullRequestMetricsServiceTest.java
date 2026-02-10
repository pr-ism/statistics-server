package com.prism.statistics.application.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedSizeMetrics;
import com.prism.statistics.domain.analysis.insight.enums.PullRequestSizeGrade;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedCommitDensityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedFileChangeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedSizeMetricsRepository;
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

    @Autowired
    private JpaPullRequestOpenedSizeMetricsRepository sizeMetricsRepository;

    @Test
    void pull_request_오픈_이벤트를_처리하면_네_종류_파생_지표가_저장된다() {
        // given
        PullRequestOpenCreatedEvent event = createPullRequestOpenCreatedEvent();

        // when
        metricsService.deriveMetrics(event);

        // then
        List<PullRequestOpenedChangeSummary> changeSummaries = changeSummaryRepository.findAll();
        List<PullRequestOpenedCommitDensity> commitDensities = commitDensityRepository.findAll();
        List<PullRequestOpenedFileChange> fileChanges = fileChangeRepository.findAll();
        List<PullRequestOpenedSizeMetrics> sizeMetrics = sizeMetricsRepository.findAll();

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
                        )),
                () -> assertThat(sizeMetrics)
                        .singleElement()
                        .satisfies(metrics -> assertAll(
                                () -> assertThat(metrics.getPullRequestId()).isEqualTo(1L),
                                () -> assertThat(metrics.getSizeScore()).isEqualByComparingTo(new BigDecimal("18.00")),
                                () -> assertThat(metrics.getSizeGrade()).isEqualTo(PullRequestSizeGrade.S),
                                () -> assertThat(metrics.getChangedFileCount()).isEqualTo(2),
                                () -> assertThat(metrics.getAddedFileCount()).isEqualTo(1),
                                () -> assertThat(metrics.getModifiedFileCount()).isEqualTo(1),
                                () -> assertThat(metrics.getRemovedFileCount()).isEqualTo(0),
                                () -> assertThat(metrics.getRenamedFileCount()).isEqualTo(0)
                        ))
        );
    }

    @Test
    void 파일_상태별_분포가_정확히_계산된다() {
        // given
        List<FileData> files = List.of(
                new FileData("src/main/java/Service.java", "modified", 10, 5),
                new FileData("src/main/java/Controller.java", "modified", 20, 0),
                new FileData("src/test/java/ServiceTest.java", "added", 15, 0),
                new FileData("docs/README.md", "removed", 0, 10),
                new FileData("OldName.java", "renamed", 0, 0)
        );
        PullRequestChangeStats changeStats = PullRequestChangeStats.create(5, 45, 15);

        PullRequestOpenCreatedEvent event = new PullRequestOpenCreatedEvent(
                2L,
                10L,
                "def456",
                PullRequestState.OPEN,
                changeStats,
                3,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                files,
                List.of()
        );

        // when
        metricsService.deriveMetrics(event);

        // then
        List<PullRequestOpenedSizeMetrics> sizeMetrics = sizeMetricsRepository.findAll();

        assertThat(sizeMetrics)
                .singleElement()
                .satisfies(metrics -> assertAll(
                        () -> assertThat(metrics.getPullRequestId()).isEqualTo(2L),
                        () -> assertThat(metrics.getSizeScore()).isEqualByComparingTo(new BigDecimal("65.00")),
                        () -> assertThat(metrics.getSizeGrade()).isEqualTo(PullRequestSizeGrade.S),
                        () -> assertThat(metrics.getChangedFileCount()).isEqualTo(5),
                        () -> assertThat(metrics.getAddedFileCount()).isEqualTo(1),
                        () -> assertThat(metrics.getModifiedFileCount()).isEqualTo(2),
                        () -> assertThat(metrics.getRemovedFileCount()).isEqualTo(1),
                        () -> assertThat(metrics.getRenamedFileCount()).isEqualTo(1)
                ));
    }

    @Test
    void 크기_등급이_임계값에_따라_올바르게_분류된다() {
        // given - XS: <10, S: <100, M: <300, L: <1000, XL: >=1000

        // XS 등급 (totalChanges = 5)
        PullRequestChangeStats xsStats = PullRequestChangeStats.create(1, 3, 2);
        PullRequestOpenCreatedEvent xsEvent = createEventWithStats(1L, xsStats);

        // M 등급 (totalChanges = 200)
        PullRequestChangeStats mStats = PullRequestChangeStats.create(5, 150, 50);
        PullRequestOpenCreatedEvent mEvent = createEventWithStats(2L, mStats);

        // XL 등급 (totalChanges = 1500)
        PullRequestChangeStats xlStats = PullRequestChangeStats.create(10, 1000, 500);
        PullRequestOpenCreatedEvent xlEvent = createEventWithStats(3L, xlStats);

        // when
        metricsService.deriveMetrics(xsEvent);
        metricsService.deriveMetrics(mEvent);
        metricsService.deriveMetrics(xlEvent);

        // then
        List<PullRequestOpenedSizeMetrics> allMetrics = sizeMetricsRepository.findAll();

        assertThat(allMetrics).hasSize(3);

        assertThat(allMetrics)
                .filteredOn(m -> m.getPullRequestId().equals(1L))
                .singleElement()
                .satisfies(m -> assertThat(m.getSizeGrade()).isEqualTo(PullRequestSizeGrade.XS));

        assertThat(allMetrics)
                .filteredOn(m -> m.getPullRequestId().equals(2L))
                .singleElement()
                .satisfies(m -> assertThat(m.getSizeGrade()).isEqualTo(PullRequestSizeGrade.M));

        assertThat(allMetrics)
                .filteredOn(m -> m.getPullRequestId().equals(3L))
                .singleElement()
                .satisfies(m -> assertThat(m.getSizeGrade()).isEqualTo(PullRequestSizeGrade.XL));
    }

    @Test
    void 크기_점수는_가중치가_적용되어_계산된다() {
        // given
        // additions=100, deletions=50, changedFileCount=10
        // 기본 가중치(1.0) 적용: 100*1 + 50*1 + 10*1 = 160
        PullRequestChangeStats stats = PullRequestChangeStats.create(10, 100, 50);
        List<FileData> files = List.of(
                new FileData("file1.java", "modified", 50, 25),
                new FileData("file2.java", "modified", 50, 25)
        );

        PullRequestOpenCreatedEvent event = new PullRequestOpenCreatedEvent(
                4L,
                10L,
                "score123",
                PullRequestState.OPEN,
                stats,
                2,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                files,
                List.of()
        );

        // when
        metricsService.deriveMetrics(event);

        // then
        List<PullRequestOpenedSizeMetrics> sizeMetrics = sizeMetricsRepository.findAll();

        assertThat(sizeMetrics)
                .singleElement()
                .satisfies(metrics -> assertAll(
                        () -> assertThat(metrics.getSizeScore()).isEqualByComparingTo(new BigDecimal("160.00")),
                        () -> assertThat(metrics.getSizeGrade()).isEqualTo(PullRequestSizeGrade.M)
                ));
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
                "abc123",
                PullRequestState.OPEN,
                changeStats,
                4,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                files,
                List.of()
        );
    }

    private PullRequestOpenCreatedEvent createEventWithStats(Long pullRequestId, PullRequestChangeStats stats) {
        List<FileData> files = List.of(
                new FileData("file.java", "modified", stats.getAdditionCount(), stats.getDeletionCount())
        );

        return new PullRequestOpenCreatedEvent(
                pullRequestId,
                10L,
                "commit" + pullRequestId,
                PullRequestState.OPEN,
                stats,
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                files,
                List.of()
        );
    }
}
