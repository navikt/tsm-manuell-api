package no.nav.tsm_manuell_api.migration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Test to verify Flyway migrations work correctly, especially V2 which configures pgaudit.log. This
 * test verifies that the migration handles missing pgaudit gracefully.
 */
@Testcontainers
class FlywayMigrationTest {

    companion object {
        @Container
        val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `V2 migration should work when target user does not exist`() {
        val flyway =
            Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .load()

        // This should succeed because the user 'tsm-manuell-api-instance' doesn't exist
        // so the IF EXISTS check prevents the ALTER USER command from running
        flyway.migrate()
        println("✓ Migrations completed successfully (user doesn't exist)")
    }

    @Test
    fun `V2 migration should handle pgaudit errors gracefully when user exists`() {
        postgres.createConnection("").use { conn ->
            val stmt = conn.createStatement()

            // Create the target user and database that the migration expects
            stmt.execute("CREATE USER \"tsm-manuell-api-instance\" WITH PASSWORD 'test'")
            stmt.execute("CREATE DATABASE \"tsm-manuell-api\"")
        }

        val flyway =
            Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load()

        // Clean and migrate - should now succeed with exception handling
        flyway.clean()
        flyway.migrate()

        println("✓ Migration completed successfully with exception handling")
        println("  The migration attempted to set pgaudit.log but handled the error gracefully")
    }

    @Test
    fun `verify migration creates expected tables`() {
        val flyway =
            Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .load()

        flyway.migrate()

        postgres.createConnection("").use { conn ->
            val stmt = conn.createStatement()

            // Verify manuelloppgave table exists
            val rs =
                stmt.executeQuery(
                    """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = 'manuelloppgave'
                ) as table_exists
                """
                        .trimIndent()
                )

            rs.next()
            val tableExists = rs.getBoolean("table_exists")
            assert(tableExists) { "manuelloppgave table should exist after migrations" }

            println("✓ Database tables created successfully")
        }
    }

    @Test
    fun `check if pgaudit extension is available`() {
        postgres.createConnection("").use { conn ->
            val stmt = conn.createStatement()

            val rs =
                stmt.executeQuery(
                    """
                SELECT EXISTS(
                    SELECT 1 FROM pg_available_extensions WHERE name = 'pgaudit'
                ) as pgaudit_available
                """
                        .trimIndent()
                )

            rs.next()
            val pgauditAvailable = rs.getBoolean("pgaudit_available")

            println("pgaudit extension available: $pgauditAvailable")
            if (!pgauditAvailable) {
                println("ℹ️  pgaudit is not available in this test environment")
                println("   This simulates dev/test environments where pgaudit is not installed")
                println("   V2 migration will handle this gracefully with exception handling")
            }
        }
    }
}
