package com.moh.yehia.catalog.service.flyway;

import com.moh.yehia.catalog.service.config.BasePostgresqlContainer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

@SpringBootTest
class FlywayIntegrationTest extends BasePostgresqlContainer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Migration versions from migration scripts
    static Stream<String> migrationVersions() {
        return Stream.of("1", "2", "3");
    }

    // All columns that should exist in the 'book' table after all migrations
    static Stream<String> bookTableColumns() {
        return Stream.of(
                "id", "author", "isbn", "price", "title", "created_date", "last_modified_date", "version",
                "publisher", "created_by", "last_modified_by"
        );
    }

    @Test
    void whenContextLoads_thenFlywayMigrationsAreApplied() {
        Integer flywayTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'flyway_schema_history'", Integer.class);
        Assertions.assertThat(flywayTableCount).isEqualTo(1);

        Integer recordsCount = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history", Integer.class);
        Assertions.assertThat(recordsCount).isEqualTo(3); // Should match number of migration scripts
    }

    @ParameterizedTest
    @MethodSource("migrationVersions")
    void givenMigrationVersion_whenCheckHistory_thenVersionExists(String version) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = ?", Integer.class, version);
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void givenFlywayMigrationsApplied_whenCheckHistory_thenNoFailedMigrations() {
        Integer failedMigrationsCount = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = false", Integer.class);
        Assertions.assertThat(failedMigrationsCount).isZero();
    }

    @Test
    void whenFlywayMigrationsApplied_thenBookTableExists() {
        Integer tablesCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'book'", Integer.class);
        Assertions.assertThat(tablesCount).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("bookTableColumns")
    void givenBookTable_whenCheckColumns_thenColumnExists(String columnName) {
        Integer columnsCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'book' and column_name = ?", Integer.class, columnName);
        Assertions.assertThat(columnsCount).isEqualTo(1);
    }

    @Test
    void givenBookTable_whenCheckAllColumns_thenAllColumnsExist() {
        List<String> expectedColumns = bookTableColumns().toList();
        Integer columnsCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'book' and column_name in (" +
                        String.join(",", expectedColumns.stream().map(c -> "'" + c + "'").toList()) + ")", Integer.class);
        Assertions.assertThat(columnsCount).isEqualTo(expectedColumns.size());
    }
}
