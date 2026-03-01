package resto_dev.modules.adminsaas.members.application.query;

import lombok.Builder;
import java.util.UUID;

/**
 * CQRS Query object holding the search filters for organization members.
 */
@Builder
public record GetOrganizationMembersQuery(
        UUID organizationId,
        int page,
        int size,
        String search,
        Boolean isActive) {
}
