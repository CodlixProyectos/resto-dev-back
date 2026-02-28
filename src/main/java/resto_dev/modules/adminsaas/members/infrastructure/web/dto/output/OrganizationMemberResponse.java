package resto_dev.modules.adminsaas.members.infrastructure.web.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Web response DTO representing an organization member.
 */
public record OrganizationMemberResponse(
        UUID id,
        UUID organizationId,
        UUID userId,
        UUID roleId,
        boolean active,
        LocalDateTime joinedAt) {
}
