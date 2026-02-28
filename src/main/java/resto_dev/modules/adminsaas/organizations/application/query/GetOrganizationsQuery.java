package resto_dev.modules.adminsaas.organizations.application.query;

import lombok.Builder;

/**
 * CQRS Query object holding the search filters for organizations.
 */
@Builder
public record GetOrganizationsQuery(
        int page,
        int size,
        String search,
        Boolean isActive) {
}
