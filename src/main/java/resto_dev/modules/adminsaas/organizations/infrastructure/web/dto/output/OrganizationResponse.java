package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output;

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
                boolean active,
                String legalName,
                String businessId,
                String email,
                String phone,
                String address,
                String logoUrl) {
}
