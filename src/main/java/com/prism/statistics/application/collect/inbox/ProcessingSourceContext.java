package com.prism.statistics.application.collect.inbox;

import org.springframework.stereotype.Component;

@Component
public class ProcessingSourceContext {

    private static final ThreadLocal<Boolean> INBOX_PROCESSING = new ThreadLocal<>();

    public boolean isInboxProcessing() {
        return Boolean.TRUE.equals(INBOX_PROCESSING.get());
    }

    public void withInboxProcessing(Runnable action) {
        Boolean previous = INBOX_PROCESSING.get();
        INBOX_PROCESSING.set(true);
        try {
            action.run();
        } finally {
            restore(previous);
        }
    }

    private void restore(Boolean previous) {
        if (previous == null) {
            INBOX_PROCESSING.remove();
            return;
        }
        INBOX_PROCESSING.set(previous);
    }
}
