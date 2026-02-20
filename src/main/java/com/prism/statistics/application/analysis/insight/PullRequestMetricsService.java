package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedCommitDensityRepository;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedFileChangeRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
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

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestFileRepository pullRequestFileRepository;
    private final PullRequestOpenedFileChangeRepository fileChangeRepository;
    private final PullRequestOpenedChangeSummaryRepository changeSummaryRepository;
    private final PullRequestOpenedCommitDensityRepository commitDensityRepository;

    @Transactional
    public void deriveMetrics(Long pullRequestId) {
        PullRequest pullRequest = pullRequestRepository.findById(pullRequestId)
                .orElseThrow(() -> new IllegalArgumentException("PullRequest not found: " + pullRequestId));
        List<PullRequestFile> files = pullRequestFileRepository.findAllByPullRequestId(pullRequestId);

        PullRequestOpenedChangeSummary changeSummary = createChangeSummary(pullRequest);
        PullRequestOpenedCommitDensity commitDensity = createCommitDensity(pullRequest);
        List<PullRequestOpenedFileChange> fileChanges = createFileChanges(pullRequestId, files);

        changeSummaryRepository.save(changeSummary);
        commitDensityRepository.save(commitDensity);
        fileChangeRepository.saveAll(fileChanges);
    }

    private PullRequestOpenedChangeSummary createChangeSummary(PullRequest pullRequest) {
        PullRequestChangeStats stats = pullRequest.getChangeStats();
        int totalChanges = stats.getAdditionCount() + stats.getDeletionCount();
        BigDecimal avgChangesPerFile = divideOrZero(totalChanges, stats.getChangedFileCount(), 4);

        return PullRequestOpenedChangeSummary.create(
                pullRequest.getId(),
                totalChanges,
                avgChangesPerFile
        );
    }

    private PullRequestOpenedCommitDensity createCommitDensity(PullRequest pullRequest) {
        PullRequestChangeStats stats = pullRequest.getChangeStats();
        int totalChanges = stats.getAdditionCount() + stats.getDeletionCount();

        BigDecimal densityPerFile = divideOrZero(pullRequest.getCommitCount(), stats.getChangedFileCount(), 4);
        BigDecimal densityPerChange = divideOrZero(pullRequest.getCommitCount(), totalChanges, 6);

        return PullRequestOpenedCommitDensity.create(
                pullRequest.getId(),
                densityPerFile,
                densityPerChange
        );
    }

    private List<PullRequestOpenedFileChange> createFileChanges(Long pullRequestId, List<PullRequestFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        Map<FileChangeType, Integer> typeCounts = countFileChangeTypes(files);
        int total = typeCounts.values().stream().mapToInt(Integer::intValue).sum();

        return typeCounts.entrySet()
                         .stream()
                         .filter(entry -> entry.getValue() > 0)
                         .map(entry -> PullRequestOpenedFileChange.create(
                                 pullRequestId,
                                 entry.getKey(),
                                 entry.getValue(),
                                 calculateRatio(entry.getValue(), total)
                         ))
                         .toList();
    }

    private Map<FileChangeType, Integer> countFileChangeTypes(List<PullRequestFile> files) {
        Map<FileChangeType, Integer> counts = new EnumMap<>(FileChangeType.class);
        for (FileChangeType type : FileChangeType.values()) {
            counts.put(type, 0);
        }
        for (PullRequestFile file : files) {
            FileChangeType type = file.getChangeType();
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
