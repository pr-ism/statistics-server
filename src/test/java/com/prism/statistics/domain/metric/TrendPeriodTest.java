package com.prism.statistics.domain.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TrendPeriodTest {

    @Nested
    class periodStartOf_메서드는 {

        @Test
        void WEEKLY일_때_주중_날짜가_속한_주의_월요일을_반환한다() {
            // given
            LocalDate wednesday = LocalDate.of(2024, 1, 3);

            // when
            LocalDate result = TrendPeriod.WEEKLY.periodStartOf(wednesday);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        void WEEKLY일_때_월요일이면_그대로_반환한다() {
            // given
            LocalDate monday = LocalDate.of(2024, 1, 1);

            // when
            LocalDate result = TrendPeriod.WEEKLY.periodStartOf(monday);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        void WEEKLY일_때_일요일이면_이전_주_월요일을_반환한다() {
            // given
            LocalDate sunday = LocalDate.of(2024, 1, 7);

            // when
            LocalDate result = TrendPeriod.WEEKLY.periodStartOf(sunday);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        void MONTHLY일_때_월중_날짜가_속한_월의_1일을_반환한다() {
            // given
            LocalDate midMonth = LocalDate.of(2024, 1, 15);

            // when
            LocalDate result = TrendPeriod.MONTHLY.periodStartOf(midMonth);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        void MONTHLY일_때_1일이면_그대로_반환한다() {
            // given
            LocalDate firstDay = LocalDate.of(2024, 3, 1);

            // when
            LocalDate result = TrendPeriod.MONTHLY.periodStartOf(firstDay);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 3, 1));
        }
    }

    @Nested
    class nextPeriodStart_메서드는 {

        @Test
        void WEEKLY일_때_7일_뒤를_반환한다() {
            // given
            LocalDate monday = LocalDate.of(2024, 1, 1);

            // when
            LocalDate result = TrendPeriod.WEEKLY.nextPeriodStart(monday);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 8));
        }

        @Test
        void MONTHLY일_때_다음_달_1일을_반환한다() {
            // given
            LocalDate january = LocalDate.of(2024, 1, 1);

            // when
            LocalDate result = TrendPeriod.MONTHLY.nextPeriodStart(january);

            // then
            assertThat(result).isEqualTo(LocalDate.of(2024, 2, 1));
        }
    }

    @Nested
    class generatePeriodStarts_메서드는 {

        @Test
        void WEEKLY일_때_범위_내_모든_주_시작일을_생성한다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 22);

            // when
            List<LocalDate> result = TrendPeriod.WEEKLY.generatePeriodStarts(startDate, endDate);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(4),
                    () -> assertThat(result.get(0)).isEqualTo(LocalDate.of(2024, 1, 1)),
                    () -> assertThat(result.get(1)).isEqualTo(LocalDate.of(2024, 1, 8)),
                    () -> assertThat(result.get(2)).isEqualTo(LocalDate.of(2024, 1, 15)),
                    () -> assertThat(result.get(3)).isEqualTo(LocalDate.of(2024, 1, 22))
            );
        }

        @Test
        void WEEKLY일_때_주중_날짜로_시작해도_해당_주의_월요일부터_생성한다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 3);
            LocalDate endDate = LocalDate.of(2024, 1, 10);

            // when
            List<LocalDate> result = TrendPeriod.WEEKLY.generatePeriodStarts(startDate, endDate);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result.get(0)).isEqualTo(LocalDate.of(2024, 1, 1)),
                    () -> assertThat(result.get(1)).isEqualTo(LocalDate.of(2024, 1, 8))
            );
        }

        @Test
        void MONTHLY일_때_범위_내_모든_월_시작일을_생성한다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);

            // when
            List<LocalDate> result = TrendPeriod.MONTHLY.generatePeriodStarts(startDate, endDate);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(3),
                    () -> assertThat(result.get(0)).isEqualTo(LocalDate.of(2024, 1, 1)),
                    () -> assertThat(result.get(1)).isEqualTo(LocalDate.of(2024, 2, 1)),
                    () -> assertThat(result.get(2)).isEqualTo(LocalDate.of(2024, 3, 1))
            );
        }

        @Test
        void 시작일과_종료일이_같은_기간에_속하면_하나의_기간만_생성한다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 3);
            LocalDate endDate = LocalDate.of(2024, 1, 5);

            // when
            List<LocalDate> result = TrendPeriod.WEEKLY.generatePeriodStarts(startDate, endDate);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(1),
                    () -> assertThat(result.get(0)).isEqualTo(LocalDate.of(2024, 1, 1))
            );
        }
    }
}
