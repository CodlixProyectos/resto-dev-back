package resto_dev.shared.tenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Manages PostgreSQL schemas for multi-tenancy.
 * Creates/drops schemas for each tenant (restaurant).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Create a new schema for a tenant.
     * Called automatically when a restaurant is created.
     *
     * @param schemaName e.g. "client_a1b2c3d4e5f6"
     */
    public void createSchema(String schemaName) {
        validateSchemaName(schemaName);

        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        jdbcTemplate.execute(sql);
        log.info("✅ Created tenant schema: {}", schemaName);
    }

    /**
     * Check if a schema exists.
     */
    public boolean schemaExists(String schemaName) {
        String sql = "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schemaName);
        return count != null && count > 0;
    }

    /**
     * Drop a schema (use with caution — for cleanup only).
     */
    public void dropSchema(String schemaName) {
        validateSchemaName(schemaName);

        String sql = "DROP SCHEMA IF EXISTS " + schemaName + " CASCADE";
        jdbcTemplate.execute(sql);
        log.warn("🗑️ Dropped tenant schema: {}", schemaName);
    }

    /**
     * Prevent SQL injection — only allow valid schema names.
     */
    private void validateSchemaName(String schemaName) {
        if (schemaName == null || !schemaName.matches("^client_[a-z0-9]{1,20}$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
    }
}
