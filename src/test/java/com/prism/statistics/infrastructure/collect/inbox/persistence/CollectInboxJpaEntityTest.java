package com.prism.statistics.infrastructure.collect.inbox.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureType;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxStatus;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.common.BoxEventTime;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureSnapshot;
import com.prism.statistics.infrastructure.common.BoxProcessingLease;
import java.time.Instant;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxJpaEntityTest {

    private static final Instant FAILED_AT = Instant.parse("2026-03-16T00:02:00Z");

    @IntegrationTest
    @Nested
    class toDomain_통합_테스트 {

        @Autowired
        private JpaCollectInboxRepository jpaCollectInboxRepository;

        @Sql("/sql/collect/insert_pending_inbox.sql")
        @Test
        void toDomain은_null_컬럼을_absent_vo로_변환한다() {
            // given
            CollectInboxJpaEntity entity = jpaCollectInboxRepository.findById(1L).orElseThrow();

            // when
            CollectInbox inbox = entity.toDomain();

            // then
            assertAll(
                    () -> assertThat(inbox.getId()).isEqualTo(1L),
                    () -> assertThat(inbox.getCollectType()).isEqualTo(CollectInboxType.PULL_REQUEST_OPENED),
                    () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PENDING),
                    () -> assertThat(inbox.getProcessingLease().isClaimed()).isFalse(),
                    () -> assertThat(inbox.getProcessedTime().isPresent()).isFalse(),
                    () -> assertThat(inbox.getFailedTime().isPresent()).isFalse(),
                    () -> assertThat(inbox.getFailure().isPresent()).isFalse()
            );
        }

        @Sql("/sql/collect/insert_failed_inbox.sql")
        @Test
        void toDomain은_present_vo를_복원한다() {
            // given
            CollectInboxJpaEntity entity = jpaCollectInboxRepository.findById(1L).orElseThrow();

            // when
            CollectInbox inbox = entity.toDomain();

            // then
            assertAll(
                    () -> assertThat(inbox.getFailedTime().occurredAt()).isEqualTo(FAILED_AT),
                    () -> assertThat(inbox.getFailure().reason()).isEqualTo("최종 실패"),
                    () -> assertThat(inbox.getFailure().type()).isEqualTo(CollectInboxFailureType.RETRY_EXHAUSTED)
            );
        }
    }

    @Test
    void toDomain_시_failureReason만_있고_failureType이_null이면_예외가_발생한다() {
        // given
        CollectInboxJpaEntity entity = createFailedEntity(null, 20L, 1);
        ReflectionTestUtils.setField(entity, "failedAt", FAILED_AT);
        ReflectionTestUtils.setField(entity, "failureReason", "실패");

        // when & then
        assertThatThrownBy(entity::toDomain)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void apply는_absent_vo를_null_컬럼으로_반영한다() {
        // given
        CollectInboxJpaEntity entity = new CollectInboxJpaEntity();
        CollectInbox inbox = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                10L,
                "{}"
        );

        // when
        entity.apply(inbox);

        // then
        assertAll(
                () -> assertThat(ReflectionTestUtils.getField(entity, "processingStartedAt")).isNull(),
                () -> assertThat(ReflectionTestUtils.getField(entity, "processedAt")).isNull(),
                () -> assertThat(ReflectionTestUtils.getField(entity, "failedAt")).isNull(),
                () -> assertThat(ReflectionTestUtils.getField(entity, "failureReason")).isNull(),
                () -> assertThat(ReflectionTestUtils.getField(entity, "failureType")).isNull()
        );
    }

    @Test
    void apply는_present_vo를_컬럼에_반영한다() {
        // given
        CollectInboxJpaEntity entity = new CollectInboxJpaEntity();
        CollectInbox inbox = CollectInbox.rehydrateBuilder()
                .id(3L)
                .collectType(CollectInboxType.PULL_REQUEST_OPENED)
                .projectId(1L)
                .runId(30L)
                .payloadJson("{}")
                .status(CollectInboxStatus.RETRY_PENDING)
                .processingAttempt(2)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.present(FAILED_AT))
                .failure(CollectInboxFailureSnapshot.present("타임아웃", CollectInboxFailureType.PROCESSING_TIMEOUT))
                .build();

        // when
        entity.apply(inbox);

        // then
        assertAll(
                () -> assertThat(ReflectionTestUtils.getField(entity, "processingStartedAt")).isNull(),
                () -> assertThat(ReflectionTestUtils.getField(entity, "processedAt")).isNull(),
                () -> assertThat(ReflectionTestUtils.getField(entity, "failedAt")).isEqualTo(FAILED_AT),
                () -> assertThat(ReflectionTestUtils.getField(entity, "failureReason")).isEqualTo("타임아웃"),
                () -> assertThat(ReflectionTestUtils.getField(entity, "failureType"))
                        .isEqualTo(CollectInboxFailureType.PROCESSING_TIMEOUT)
        );
    }

    private CollectInboxJpaEntity createFailedEntity(Long id, long runId, int processingAttempt) {
        CollectInboxJpaEntity entity = new CollectInboxJpaEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "collectType", CollectInboxType.PULL_REQUEST_OPENED);
        ReflectionTestUtils.setField(entity, "projectId", 1L);
        ReflectionTestUtils.setField(entity, "runId", runId);
        ReflectionTestUtils.setField(entity, "payloadJson", "{}");
        ReflectionTestUtils.setField(entity, "status", CollectInboxStatus.FAILED);
        ReflectionTestUtils.setField(entity, "processingAttempt", processingAttempt);
        return entity;
    }
}
