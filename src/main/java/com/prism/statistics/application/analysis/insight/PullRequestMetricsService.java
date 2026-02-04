package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedCommitDensityRepository;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedFileChangeRepository;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PullRequestMetricsService {

    private final PullRequestOpenedFileChangeRepository fileChangeRepository;
    private final PullRequestOpenedChangeSummaryRepository changeSummaryRepository;
    private final PullRequestOpenedCommitDensityRepository commitDensityRepository;

    @Transactional
    public void deriveMetrics(PullRequestOpenCreatedEvent event) {
        PullRequestOpenedChangeSummary changeSummary = createChangeSummary(event);
        PullRequestOpenedCommitDensity commitDensity = createCommitDensity(event);
        List<PullRequestOpenedFileChange> fileChanges = createFileChanges(event);

        changeSummaryRepository.save(changeSummary);
        commitDensityRepository.save(commitDensity);
        fileChangeRepository.saveAll(fileChanges);
    }

    private PullRequestOpenedChangeSummary createChangeSummary(PullRequestOpenCreatedEvent event) {
        PullRequestChangeStats stats = event.changeStats();
        int totalChanges = stats.getAdditionCount() + stats.getDeletionCount();
        BigDecimal avgChangesPerFile = divideOrZero(totalChanges, stats.getChangedFileCount(), 4);

        return PullRequestOpenedChangeSummary.create(
                event.pullRequestId(),
                totalChanges,
                avgChangesPerFile
        );
    }

    private PullRequestOpenedCommitDensity createCommitDensity(PullRequestOpenCreatedEvent event) {
        PullRequestChangeStats stats = event.changeStats();
        int totalChanges = stats.getAdditionCount() + stats.getDeletionCount();

        BigDecimal densityPerFile = divideOrZero(event.commitCount(), stats.getChangedFileCount(), 4);
        BigDecimal densityPerChange = divideOrZero(event.commitCount(), totalChanges, 6);

        return PullRequestOpenedCommitDensity.create(
                event.pullRequestId(),
                densityPerFile,
                densityPerChange
        );
    }

    private List<PullRequestOpenedFileChange> createFileChanges(
            PullRequestOpenCreatedEvent event
    ) {
        List<FileData> files = event.files();
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        Map<FileChangeType, Integer> typeCounts = countFileChangeTypes(files);
        int total = typeCounts.values().stream().mapToInt(Integer::intValue).sum();

        return typeCounts.entrySet()
                         .stream()
                         .filter(entry -> entry.getValue() > 0)
                         .map(entry -> PullRequestOpenedFileChange.create(
                                 event.pullRequestId(),
                                 entry.getKey(),
                                 entry.getValue(),
                                 calculateRatio(entry.getValue(), total)
                         ))
                         .toList();
    }

    private Map<FileChangeType, Integer> countFileChangeTypes(List<FileData> files) {
        Map<FileChangeType, Integer> counts = new EnumMap<>(FileChangeType.class);
        for (FileChangeType type : FileChangeType.values()) {
            counts.put(type, 0);
        }
        for (FileData file : files) {
            FileChangeType type = FileChangeType.fromGitHubStatus(file.status());
            counts.put(type, counts.get(type) + 1);
        }
        return counts;
    }

    private BigDecimal divideOrZero(int numerator, int denominator, int scale) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                         .divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRatio(int count, int total) {
        return divideOrZero(count, total, 2);
    }
}
