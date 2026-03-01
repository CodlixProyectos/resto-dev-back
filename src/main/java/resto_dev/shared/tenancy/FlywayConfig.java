package resto_dev.shared.tenancy;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Configuration
public class FlywayConfig {

    /**
     * By naming this bean "flyway", Spring Boot's JPA auto-configuration will
     * automatically ensure that this bean is initialized BEFORE the
     * EntityManagerFactory.
     * This guarantees that our tables exist before Hibernate validates them.
     */
    @Bean
    public Flyway flyway(DataSource dataSource) {
        log.info("Starting Flyway Database Migrations...");

        // 1. Migrate Global Admin Schema
        Flyway flywayAdmin = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/admin")
                .schemas("admin")
                .defaultSchema("admin")
                .baselineOnMigrate(true)
                .load();

        flywayAdmin.migrate();
        log.info("Admin schema migration completed successfully.");

        // 2. Migrate All Existing Tenant Schemas
        try (Connection connection = dataSource.getConnection();
                ResultSet rs = connection.createStatement()
                        .executeQuery("SELECT schema_name FROM admin.organizations")) {

            while (rs.next()) {
                String tenantSchema = rs.getString("schema_name");
                migrateTenantSchema(dataSource, tenantSchema);
            }

        } catch (SQLException e) {
            log.error("Failed to query existing organizations to perform tenant migrations.", e);
        }

        return flywayAdmin;
    }

    /**
     * Run migration for a specific tenant schema.
     * Can be called dynamically when a new Organization is registered.
     */
    public void migrateTenantSchema(DataSource dataSource, String tenantSchema) {
        log.info("Migrating schema for tenant: {}", tenantSchema);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE SCHEMA IF NOT EXISTS \"" + tenantSchema + "\"");
        } catch (SQLException e) {
            log.error("Failed to create schema: {}", tenantSchema, e);
            throw new RuntimeException("Schema creation failed for " + tenantSchema, e);
        }

        Flyway flywayTenant = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/tenants")
                .schemas(tenantSchema)
                .defaultSchema(tenantSchema)
                .baselineOnMigrate(true)
                .load();

        flywayTenant.migrate();
        log.info("Tenant schema migration completed for: {}", tenantSchema);
    }
}
