package resto_dev.shared.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // Seed Admin Organization if missing (in admin schema)
            Integer orgCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM admin.organizations WHERE id = '605241c3-3551-4636-91e5-c743b521e082'", Integer.class);
            
            if (orgCount == 0) {
                log.info("Seeding test organization 'Codlix Restaurant'...");
                jdbcTemplate.execute("INSERT INTO admin.organizations (id, name, slug, schema_name, active) " +
                    "VALUES ('605241c3-3551-4636-91e5-c743b521e082', 'Codlix Restaurant', 'codlix-restaurant', 'codlix', true)");
            }

            // Seed Categories and Products in 'codlix' schema
            seedCodlixSchema();

        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }

    private void seedCodlixSchema() {
        try {
            // Ensure schema exists (it should from Flyway, but let's be safe)
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS codlix");
            
            // We use the tables created by Flyway in the 'codlix' schema
            Integer catCount = jdbcTemplate.queryForObject("SELECT count(*) FROM codlix.category", Integer.class);
            
            if (catCount == 0) {
                log.info("Seeding categories and products for 'codlix' schema...");
                
                UUID cat1Id = UUID.randomUUID();
                UUID cat2Id = UUID.randomUUID();

                jdbcTemplate.execute(String.format(
                    "INSERT INTO codlix.category (id, name, description, active) VALUES ('%s', 'Entradas', 'Platos para empezar', true)", cat1Id));
                jdbcTemplate.execute(String.format(
                    "INSERT INTO codlix.category (id, name, description, active) VALUES ('%s', 'Platos de Fondo', 'Nuestros mejores platos', true)", cat2Id));

                jdbcTemplate.execute(String.format(
                    "INSERT INTO codlix.product (id, category_id, name, description, price, active) VALUES ('%s', '%s', 'Cebiche Clásico', 'Pescado fresco marinado', 35.00, true)",
                    UUID.randomUUID(), cat1Id));
                
                jdbcTemplate.execute(String.format(
                    "INSERT INTO codlix.product (id, category_id, name, description, price, active) VALUES ('%s', '%s', 'Lomo Saltado', 'Carne de res salteada al wok', 45.00, true)",
                    UUID.randomUUID(), cat2Id));

                log.info("Codlix schema seeded successfully!");
            }
        } catch (Exception e) {
            log.warn("Could not seed codlix schema (maybe it doesn't exist yet): {}", e.getMessage());
        }
    }
}
