package ru.pyatkinmv.pognaleey;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public abstract class DatabaseCleaningTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        var truncateQuery = """
                DO $$
                DECLARE
                    table_name text;
                BEGIN
                    FOR table_name IN
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public' AND tablename != 'flyway_schema_history'
                    LOOP
                        EXECUTE format('TRUNCATE TABLE %I CASCADE', table_name);
                    END LOOP;
                END $$;
                """;
        jdbcTemplate.execute(truncateQuery);
    }
}
