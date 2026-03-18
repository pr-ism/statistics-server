package com.prism.statistics.application.collect.inbox;

import java.util.Set;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionTimedOutException;

@Component
public class CollectRetryExceptionClassifier {

    private static final Set<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS = Set.of(
            TransientDataAccessException.class,
            TransactionTimedOutException.class
    );

    public boolean isRetryable(Throwable throwable) {
        Throwable cursor = throwable;

        while (cursor != null) {
            if (isRetryableType(cursor)) {
                return true;
            }

            Throwable next = cursor.getCause();
            if (next == cursor) {
                break;
            }
            cursor = next;
        }

        return false;
    }

    private boolean isRetryableType(Throwable throwable) {
        for (Class<? extends Throwable> retryableClass : RETRYABLE_EXCEPTIONS) {
            if (retryableClass.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return false;
    }
}
