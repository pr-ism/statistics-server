package com.prism.statistics.context;

import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.sql.SQLException;

@TestConfiguration
public class TestDuplicateKeyConfiguration {

    @Bean
    @Primary
    public MysqlDuplicateKeyDetector mysqlDuplicateKeyDetector() {
        return new MysqlDuplicateKeyDetector() {

            @Override
            public boolean isDuplicateKey(Throwable throwable) {
                if (isH2DuplicateKey(throwable)) {
                    return true;
                }
                return super.isDuplicateKey(throwable);
            }

            private boolean isH2DuplicateKey(Throwable throwable) {
                Throwable current = throwable;

                while (current != null) {
                    if (current instanceof ConstraintViolationException cve && isH2SqlState(cve.getSQLException())) {
                        return true;
                    }

                    if (current instanceof SQLException sqlException && isH2SqlState(sqlException)) {
                        return true;
                    }

                    current = current.getCause();
                }

                return false;
            }

            private boolean isH2SqlState(SQLException sqlException) {
                if (sqlException == null) {
                    return false;
                }
                return "23505".equals(sqlException.getSQLState());
            }
        };
    }
}
