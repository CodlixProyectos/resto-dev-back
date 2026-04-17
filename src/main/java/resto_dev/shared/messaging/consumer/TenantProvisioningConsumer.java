package resto_dev.shared.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.shared.messaging.RabbitMqConfig;
import resto_dev.shared.messaging.dto.TenantProvisioningMessage;
import resto_dev.shared.tenancy.FlywayConfig;

import javax.sql.DataSource;

/**
 * Este es el "Worker" que corre en segundo plano.
 * Su único trabajo es escuchar la cola de RabbitMQ y ejecutar las tareas pesadas
 * que no queríamos que el usuario esperara en el registro inicial.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantProvisioningConsumer {

    private final FlywayConfig flywayConfig;
    private final OrganizationJpaRepository organizationRepository;
    private final DataSource dataSource;

    /**
     * Este método se activa automáticamente cuando llega un mensaje a la cola.
     * @param message El DTO con la información de la organización y el esquema.
     */
    @RabbitListener(queues = RabbitMqConfig.TENANT_PROVISIONING_QUEUE)
    @Transactional
    public void handleTenantProvisioning(TenantProvisioningMessage message) {
        log.info("Worker: Iniciando provisionamiento para Org ID: {}, Schema: {}", 
                 message.organizationId(), message.schemaName());

        try {
            // 1. Ejecutar la migración pesada de Flyway
            // Esto es lo que antes bloqueaba la respuesta HTTP. Ahora ocurre aquí "sin prisas".
            flywayConfig.migrateTenantSchema(dataSource, message.schemaName());

            // 2. Si todo salió bien, activamos la organización
            organizationRepository.findById(message.organizationId()).ifPresent(org -> {
                org.setRegistrationStatus("ACTIVE");
                org.setActive(true);
                organizationRepository.save(org);
                log.info("Worker: ✅ Organización {} activada con éxito.", org.getName());
            });

        } catch (Exception e) {
            log.error("Worker: ❌ Error crítico al crear el esquema para {}: {}", 
                      message.schemaName(), e.getMessage());
            
            // 3. Si falla, marcamos el error para que el soporte pueda revisarlo
            organizationRepository.findById(message.organizationId()).ifPresent(org -> {
                org.setRegistrationStatus("FAILED_SETUP");
                organizationRepository.save(org);
            });
        }
    }
}
