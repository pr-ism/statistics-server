package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.request.SizeStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse.SizeStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SizeStatisticsQueryServiceTest {

    @Autowired
    private SizeStatisticsQueryService sizeStatisticsQueryService;

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_size_statistics.sql")
    @Test
    void 프로젝트의_PR_크기_분포_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        SizeStatisticsRequest request = new SizeStatisticsRequest(null, null);

        // when
        SizeStatisticsResponse actual = sizeStatisticsQueryService.findSizeStatistics(userId, projectId, request);

        // then
        // SMALL: 2개 (PR1: 40줄, PR2: 100줄), 평균 파일 수: (1+2)/2=1.5, 평균 커밋: (1+2)/2=1.5
        // MEDIUM: 3개 (PR3: 150줄, PR4: 300줄, PR5: 201줄), 평균 파일 수: (4+6+5)/3=5.0, 평균 커밋: (3+4+5)/3=4.0
        // LARGE: 2개 (PR6: 600줄, PR7: 700줄), 평균 파일 수: (12+14)/2=13.0, 평균 커밋: (8+10)/2=9.0
        // EXTRA_LARGE: 3개 (PR8: 800줄, PR9: 1500줄, PR10: 1100줄), 평균 파일 수: (20+30+25)/3=25.0, 평균 커밋: (12+18+15)/3=15.0
        SizeStatistics small = findByCategory(actual, "SMALL");
        SizeStatistics medium = findByCategory(actual, "MEDIUM");
        SizeStatistics large = findByCategory(actual, "LARGE");
        SizeStatistics extraLarge = findByCategory(actual, "EXTRA_LARGE");

        assertAll(
                () -> assertThat(actual.sizeStatistics()).hasSize(4),
                () -> assertThat(small.count()).isEqualTo(2),
                () -> assertThat(small.percentage()).isEqualTo(20.0),
                () -> assertThat(small.averageChangedFileCount()).isEqualTo(1.5),
                () -> assertThat(small.averageCommitCount()).isEqualTo(1.5),
                () -> assertThat(medium.count()).isEqualTo(3),
                () -> assertThat(medium.percentage()).isEqualTo(30.0),
                () -> assertThat(medium.averageChangedFileCount()).isEqualTo(5.0),
                () -> assertThat(medium.averageCommitCount()).isEqualTo(4.0),
                () -> assertThat(large.count()).isEqualTo(2),
                () -> assertThat(large.percentage()).isEqualTo(20.0),
                () -> assertThat(large.averageChangedFileCount()).isEqualTo(13.0),
                () -> assertThat(large.averageCommitCount()).isEqualTo(9.0),
                () -> assertThat(extraLarge.count()).isEqualTo(3),
                () -> assertThat(extraLarge.percentage()).isEqualTo(30.0),
                () -> assertThat(extraLarge.averageChangedFileCount()).isEqualTo(25.0),
                () -> assertThat(extraLarge.averageCommitCount()).isEqualTo(15.0)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PR이_없는_프로젝트의_크기_분포를_조회하면_4개_구간_모두_count가_0이다() {
        // given
        Long userId = 1L;
        Long projectId = 1L;
        SizeStatisticsRequest request = new SizeStatisticsRequest(null, null);

        // when
        SizeStatisticsResponse actual = sizeStatisticsQueryService.findSizeStatistics(userId, projectId, request);

        // then
        assertAll(
                () -> assertThat(actual.sizeStatistics()).hasSize(4),
                () -> assertThat(actual.sizeStatistics()).allSatisfy(stat -> {
                    assertThat(stat.count()).isEqualTo(0);
                    assertThat(stat.percentage()).isEqualTo(0.0);
                    assertThat(stat.averageChangedFileCount()).isEqualTo(0.0);
                    assertThat(stat.averageCommitCount()).isEqualTo(0.0);
                })
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_size_statistics_date_range.sql")
    @Test
    void 시작일과_종료일을_지정하면_해당_범위의_PR_크기_분포만_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 20);
        LocalDate endDate = LocalDate.of(2024, 2, 15);
        SizeStatisticsRequest request = new SizeStatisticsRequest(startDate, endDate);

        // when
        SizeStatisticsResponse actual = sizeStatisticsQueryService.findSizeStatistics(userId, projectId, request);

        // then
        // PR2(Jan 20, MEDIUM) + PR3(Feb 15, LARGE) 포함, PR1(Jan 10) 및 PR4(Mar 1) 제외
        SizeStatistics small = findByCategory(actual, "SMALL");
        SizeStatistics medium = findByCategory(actual, "MEDIUM");
        SizeStatistics large = findByCategory(actual, "LARGE");
        SizeStatistics extraLarge = findByCategory(actual, "EXTRA_LARGE");

        assertAll(
                () -> assertThat(small.count()).isEqualTo(0),
                () -> assertThat(medium.count()).isEqualTo(1),
                () -> assertThat(medium.percentage()).isEqualTo(50.0),
                () -> assertThat(large.count()).isEqualTo(1),
                () -> assertThat(large.percentage()).isEqualTo(50.0),
                () -> assertThat(extraLarge.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_size_statistics_date_range.sql")
    @Test
    void 시작일만_지정하면_시작일_이후의_PR_크기_분포를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate startDate = LocalDate.of(2024, 2, 15);
        SizeStatisticsRequest request = new SizeStatisticsRequest(startDate, null);

        // when
        SizeStatisticsResponse actual = sizeStatisticsQueryService.findSizeStatistics(userId, projectId, request);

        // then
        // PR3(Feb 15, LARGE) + PR4(Mar 1, EXTRA_LARGE) 포함
        SizeStatistics large = findByCategory(actual, "LARGE");
        SizeStatistics extraLarge = findByCategory(actual, "EXTRA_LARGE");

        assertAll(
                () -> assertThat(large.count()).isEqualTo(1),
                () -> assertThat(large.percentage()).isEqualTo(50.0),
                () -> assertThat(extraLarge.count()).isEqualTo(1),
                () -> assertThat(extraLarge.percentage()).isEqualTo(50.0)
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_size_statistics_date_range.sql")
    @Test
    void 종료일만_지정하면_종료일_이전의_PR_크기_분포를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        SizeStatisticsRequest request = new SizeStatisticsRequest(null, endDate);

        // when
        SizeStatisticsResponse actual = sizeStatisticsQueryService.findSizeStatistics(userId, projectId, request);

        // then
        // PR1(Jan 10, SMALL) + PR2(Jan 20, MEDIUM) 포함
        SizeStatistics small = findByCategory(actual, "SMALL");
        SizeStatistics medium = findByCategory(actual, "MEDIUM");

        assertAll(
                () -> assertThat(small.count()).isEqualTo(1),
                () -> assertThat(small.percentage()).isEqualTo(50.0),
                () -> assertThat(medium.count()).isEqualTo(1),
                () -> assertThat(medium.percentage()).isEqualTo(50.0)
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_PR_크기_분포를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;
        SizeStatisticsRequest request = new SizeStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() -> sizeStatisticsQueryService.findSizeStatistics(userId, projectId, request))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    private SizeStatistics findByCategory(SizeStatisticsResponse response, String category) {
        return response.sizeStatistics().stream()
                .filter(stat -> stat.sizeCategory().equals(category))
                .findFirst()
                .orElseThrow();
    }
}
