package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.request.TrendStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse.TrendDataPoint;
import com.prism.statistics.domain.metric.TrendPeriod;
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
class TrendStatisticsQueryServiceTest {

    @Autowired
    private TrendStatisticsQueryService trendStatisticsQueryService;

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_trend_statistics.sql")
    @Test
    void 주간_트렌드_통계를_조회하면_빈_주도_포함하여_반환한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        TrendStatisticsRequest request = new TrendStatisticsRequest(
                TrendPeriod.WEEKLY,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 28)
        );

        // when
        TrendStatisticsResponse actual = trendStatisticsQueryService.findTrendStatistics(userId, projectId, request);

        // then
        // 2024-01-01 주: 2건 (PR1: 150, PR2: 300) → 평균 225.0
        // 2024-01-08 주: 0건
        // 2024-01-15 주: 3건 (PR3: 80, PR4: 600, PR5: 120) → 평균 266.666...
        // 2024-01-22 주: 1건 (PR6: 1000) → 평균 1000.0
        assertAll(
                () -> assertThat(actual.period()).isEqualTo("WEEKLY"),
                () -> assertThat(actual.trends()).hasSize(4),
                () -> assertThat(actual.trends().get(0).periodStart()).isEqualTo(LocalDate.of(2024, 1, 1)),
                () -> assertThat(actual.trends().get(0).prCount()).isEqualTo(2),
                () -> assertThat(actual.trends().get(0).averageChangeAmount()).isEqualTo(225.0),
                () -> assertThat(actual.trends().get(1).periodStart()).isEqualTo(LocalDate.of(2024, 1, 8)),
                () -> assertThat(actual.trends().get(1).prCount()).isEqualTo(0),
                () -> assertThat(actual.trends().get(1).averageChangeAmount()).isEqualTo(0.0),
                () -> assertThat(actual.trends().get(2).periodStart()).isEqualTo(LocalDate.of(2024, 1, 15)),
                () -> assertThat(actual.trends().get(2).prCount()).isEqualTo(3),
                () -> assertThat(actual.trends().get(3).periodStart()).isEqualTo(LocalDate.of(2024, 1, 22)),
                () -> assertThat(actual.trends().get(3).prCount()).isEqualTo(1),
                () -> assertThat(actual.trends().get(3).averageChangeAmount()).isEqualTo(1000.0)
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_trend_statistics.sql")
    @Test
    void 월간_트렌드_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        TrendStatisticsRequest request = new TrendStatisticsRequest(
                TrendPeriod.MONTHLY,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );

        // when
        TrendStatisticsResponse actual = trendStatisticsQueryService.findTrendStatistics(userId, projectId, request);

        // then
        // 2024-01: 6건 (PR1~PR6 모두 1월)
        assertAll(
                () -> assertThat(actual.period()).isEqualTo("MONTHLY"),
                () -> assertThat(actual.trends()).hasSize(1),
                () -> assertThat(actual.trends().get(0).periodStart()).isEqualTo(LocalDate.of(2024, 1, 1)),
                () -> assertThat(actual.trends().get(0).prCount()).isEqualTo(6)
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_trend_statistics.sql")
    @Test
    void 평균_변경량이_정확하게_계산된다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        TrendStatisticsRequest request = new TrendStatisticsRequest(
                TrendPeriod.WEEKLY,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 1, 21)
        );

        // when
        TrendStatisticsResponse actual = trendStatisticsQueryService.findTrendStatistics(userId, projectId, request);

        // then
        // 2024-01-15 주: PR3(80) + PR4(600) + PR5(120) → 평균 (80+600+120)/3 = 266.666...
        TrendDataPoint dataPoint = actual.trends().get(0);

        assertAll(
                () -> assertThat(actual.trends()).hasSize(1),
                () -> assertThat(dataPoint.prCount()).isEqualTo(3),
                () -> assertThat(dataPoint.averageChangeAmount()).isCloseTo(266.67, org.assertj.core.data.Offset.offset(0.01))
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_트렌드_통계를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;
        TrendStatisticsRequest request = new TrendStatisticsRequest(
                TrendPeriod.WEEKLY,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );

        // when & then
        assertThatThrownBy(() -> trendStatisticsQueryService.findTrendStatistics(userId, projectId, request))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
