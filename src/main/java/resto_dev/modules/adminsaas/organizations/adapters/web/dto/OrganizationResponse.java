package resto_dev.modules.adminsaas.organizations.adapters.web.dto;

import java.util.UUID;

/**
 * Organization response DTO.
 */
public record OrganizationResponse(
                UUID id,
                String name,
                String slug,
                String schemaName,
                String type,
                UUID ownerId,
                boolean active) {
}
