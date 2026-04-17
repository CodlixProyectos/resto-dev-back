package resto_dev.shared.messaging.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Este objeto (DTO) representa el mensaje que se enviará a la cola.
 * Implementa Serializable para que Spring pueda convertirlo a un formato que RabbitMQ entienda.
 * 
 * @param organizationId ID único de la organización recién creada.
 * @param schemaName Nombre del esquema SQL que se debe crear (ej: client_restaurante).
 */
public record TenantProvisioningMessage(
    UUID organizationId,
    String schemaName
) implements Serializable {
}
