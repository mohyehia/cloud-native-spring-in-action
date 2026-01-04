package com.moh.yehia.catalog.service.flyway;

import com.moh.yehia.catalog.service.config.BasePostgresqlContainer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FlywayIntegrationTest extends BasePostgresqlContainer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void whenContextLoads_thenFlywayMigrationsAreApplied() {
        Integer flywayTableCount = jdbcTemplate.queryForObject("select count(*) from information_schema.tables where table_name = 'flyway_schema_history'", Integer.class);
        Assertions.assertThat(flywayTableCount).isEqualTo(1);

        Integer recordsCount = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history", Integer.class);
        Assertions.assertThat(recordsCount).isGreaterThan(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void givenMigrationVersion_whenCheckHistory_thenVersionExists(String version) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where version = ?", Integer.class, version);
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void givenFlywayMigrationsApplied_whenCheckHistory_thenNoFailedMigrations() {
        Integer failedMigrationsCount = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where success = false", Integer.class);
        Assertions.assertThat(failedMigrationsCount).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"book"})
    void whenFlywayMigrationsApplied_thenTablesExist(String tableName) {
        Integer tablesCount = jdbcTemplate.queryForObject("select count(*) from information_schema.tables where table_name = ?", Integer.class, tableName);
        Assertions.assertThat(tablesCount).isEqualTo(1);
    }

    @Test
    void givenBookTable_whenCheckColumns_thenColumnsExist() {
        Integer columnsCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'book' and column_name in ('id', 'isbn', 'author', 'price', 'title', 'publisher', 'created_date', 'last_modified_date', 'version')", Integer.class);
        Assertions.assertThat(columnsCount).isEqualTo(9);

    }

}
