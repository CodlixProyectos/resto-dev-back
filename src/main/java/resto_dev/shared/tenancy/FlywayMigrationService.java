package resto_dev.shared.tenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlywayMigrationService {

    private final DataSource dataSource;

    /**
     * Executes Flyway migrations within a specific tenant schema.
     * 
     * @param schemaName The name of the tenant's schema (e.g., tenant_client123)
     */
    public void migrateTenantSchema(String schemaName) {
        log.info("Starting Flyway migrations for tenant schema: {}", schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/tenants") // Folder for tenant scripts
                .defaultSchema(schemaName)
                .schemas(schemaName)
                .load();

        flyway.migrate();

        log.info("Successfully completed Flyway migrations for tenant schema: {}", schemaName);
    }
}
