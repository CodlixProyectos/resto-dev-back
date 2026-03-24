package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import resto_dev.modules.adminsaas.organizations.application.port.input.GetOrganizationsUseCase;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationQueryPort;
import resto_dev.modules.adminsaas.organizations.application.query.GetOrganizationsQuery;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.shared.responses.PaginatedResponse;
import org.springframework.data.domain.Page;

/**
 * Service orchestrating the retrieval of paginated and filtered organizations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationQueryService implements GetOrganizationsUseCase {

    private final OrganizationQueryPort queryPort;

    @Override
    public PaginatedResponse<Organization> getOrganizations(GetOrganizationsQuery query) {
        log.debug("Fetching organizations with query: {}", query);
        Page<Organization> results = queryPort.searchOrganizations(query);
        return PaginatedResponse.of(results);
    }
}
