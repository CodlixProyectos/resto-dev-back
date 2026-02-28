package resto_dev.modules.adminsaas.organizations.application.port.input;

import resto_dev.modules.adminsaas.organizations.application.query.GetOrganizationsQuery;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.shared.responses.PaginatedResponse;

/**
 * Inbound port to read and filter organizations.
 */
public interface GetOrganizationsUseCase {
    PaginatedResponse<Organization> getOrganizations(GetOrganizationsQuery query);
}
