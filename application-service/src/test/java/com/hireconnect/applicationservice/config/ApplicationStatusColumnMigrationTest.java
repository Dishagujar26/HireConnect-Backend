package com.hireconnect.applicationservice.config;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

class ApplicationStatusColumnMigrationTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private ApplicationStatusColumnMigration migration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void run_ShouldMigrateIfEnum() {
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(Map.of("dataType", "enum"));

        migration.run(args);

        verify(jdbcTemplate).execute(contains("ALTER TABLE job_applications"));
    }

    @Test
    void run_ShouldNotMigrateIfNotEnum() {
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(Map.of("dataType", "varchar"));

        migration.run(args);

        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void run_ShouldHandleException() {
        when(jdbcTemplate.queryForMap(anyString())).thenThrow(new RuntimeException("DB error"));

        migration.run(args);

        // Should just log and not crash
        verify(jdbcTemplate, never()).execute(anyString());
    }
}
