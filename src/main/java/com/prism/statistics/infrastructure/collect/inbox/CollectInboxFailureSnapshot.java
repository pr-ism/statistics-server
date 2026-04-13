package com.prism.statistics.infrastructure.collect.inbox;

public sealed interface CollectInboxFailureSnapshot
        permits CollectInboxFailureSnapshot.AbsentCollectInboxFailureSnapshot,
        CollectInboxFailureSnapshot.PresentCollectInboxFailureSnapshot {

    static CollectInboxFailureSnapshot absent() {
        return AbsentCollectInboxFailureSnapshot.INSTANCE;
    }

    static CollectInboxFailureSnapshot present(String reason, CollectInboxFailureType type) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("failureReason은 비어 있을 수 없습니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("failureType은 비어 있을 수 없습니다.");
        }

        return new PresentCollectInboxFailureSnapshot(reason, type);
    }

    boolean isPresent();

    default String reason() {
        throw new IllegalStateException("실패 정보가 없는 상태입니다.");
    }

    default CollectInboxFailureType type() {
        throw new IllegalStateException("실패 정보가 없는 상태입니다.");
    }

    final class AbsentCollectInboxFailureSnapshot implements CollectInboxFailureSnapshot {

        private static final AbsentCollectInboxFailureSnapshot INSTANCE =
                new AbsentCollectInboxFailureSnapshot();

        private AbsentCollectInboxFailureSnapshot() {
        }

        @Override
        public boolean isPresent() {
            return false;
        }
    }

    record PresentCollectInboxFailureSnapshot(String reason, CollectInboxFailureType type)
            implements CollectInboxFailureSnapshot {

        public PresentCollectInboxFailureSnapshot {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("failureReason은 비어 있을 수 없습니다.");
            }
            if (type == null) {
                throw new IllegalArgumentException("failureType은 비어 있을 수 없습니다.");
            }
        }

        @Override
        public boolean isPresent() {
            return true;
        }
    }
}
