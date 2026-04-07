package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaasOrganizationDeleterService {

    private final OrganizationJpaRepository organizationRepository;
    private final DataSource dataSource;

    @Transactional
    public void deactivate(UUID id) {
        OrganizationJpaEntity org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));
        
        org.setActive(false);
        organizationRepository.save(org);
        log.info("Organización desactivada: {} ({})", org.getName(), id);
    }

    @Transactional
    public void forceDelete(UUID id) {
        OrganizationJpaEntity org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));

        String schemaName = org.getSchemaName();
        log.warn("INICIANDO ELIMINACIÓN PERMANENTE de la organización: {} (Esquema: {})", org.getName(), schemaName);

        // 1. Drop the schema first (This is the most critical part)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Note: CASCADE is essential to drop all tables, views, etc. inside the schema
            String sql = "DROP SCHEMA IF EXISTS \"" + schemaName + "\" CASCADE";
            stmt.execute(sql);
            log.info("Esquema '{}' eliminado con éxito.", schemaName);
            
        } catch (Exception e) {
            log.error("Error al eliminar el esquema '{}': {}", schemaName, e.getMessage());
            throw new RuntimeException("No se pudo eliminar el esquema de la base de datos", e);
        }

        // 2. Delete the record from the admin schema
        // This will trigger CASCADE delete for subscriptions and members due to FK constraints
        organizationRepository.delete(org);
        log.info("Registro de la organización eliminado de la tabla maestra.");
    }
}
