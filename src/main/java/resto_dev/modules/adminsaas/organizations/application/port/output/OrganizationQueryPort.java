package resto_dev.modules.adminsaas.organizations.application.port.output;

import resto_dev.modules.adminsaas.organizations.application.query.GetOrganizationsQuery;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import org.springframework.data.domain.Page;

/**
 * Outbound port for reading organizations (repository query side).
 */
public interface OrganizationQueryPort {
    Page<Organization> searchOrganizations(GetOrganizationsQuery query);
}
