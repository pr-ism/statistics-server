package com.prism.statistics.infrastructure.common;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class MysqlDuplicateKeyDetector {

    public boolean isDuplicateKey(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException cve && isMysqlDuplicate(cve.getSQLException())) {
                return true;
            }
            if (current instanceof SQLException sqlException && isMysqlDuplicate(sqlException)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isMysqlDuplicate(SQLException sqlException) {
        if (sqlException == null) {
            return false;
        }
        return "23000".equals(sqlException.getSQLState()) && sqlException.getErrorCode() == 1062;
    }
}
