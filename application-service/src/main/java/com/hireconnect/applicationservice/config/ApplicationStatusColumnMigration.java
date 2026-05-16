package com.hireconnect.applicationservice.config;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationStatusColumnMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStatusColumnMigration.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Map<String, Object> columnMeta = jdbcTemplate.queryForMap(
                    "SELECT DATA_TYPE AS dataType FROM INFORMATION_SCHEMA.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() "
                            + "AND TABLE_NAME = 'job_applications' "
                            + "AND COLUMN_NAME = 'status'"
            );

            String dataType = String.valueOf(columnMeta.getOrDefault("dataType", "")).toLowerCase(Locale.ROOT);
            if ("enum".equals(dataType)) {
                jdbcTemplate.execute("ALTER TABLE job_applications MODIFY COLUMN status VARCHAR(32) NOT NULL");
                log.info("Migrated job_applications.status from ENUM to VARCHAR(32)");
            } else {
                log.info("No status column migration needed. Current data type: {}", dataType);
            }
        } catch (Exception ex) {
            log.warn("Status column migration skipped/failed: {}", ex.getMessage());
        }
    }
}
