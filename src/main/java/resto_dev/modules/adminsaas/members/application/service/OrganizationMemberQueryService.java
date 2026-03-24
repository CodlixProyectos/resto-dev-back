package resto_dev.modules.adminsaas.members.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import resto_dev.modules.adminsaas.members.application.port.input.GetOrganizationMembersUseCase;
import resto_dev.modules.adminsaas.members.application.port.output.OrganizationMemberQueryPort;
import resto_dev.modules.adminsaas.members.application.query.GetOrganizationMembersQuery;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.shared.responses.PaginatedResponse;
import org.springframework.data.domain.Page;

/**
 * Service orchestrating the retrieval of paginated and filtered organization
 * members.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationMemberQueryService implements GetOrganizationMembersUseCase {

    private final OrganizationMemberQueryPort queryPort;

    @Override
    public PaginatedResponse<OrganizationMember> getMembers(GetOrganizationMembersQuery query) {
        log.debug("Fetching members with query: {}", query);
        Page<OrganizationMember> results = queryPort.searchMembers(query);
        return PaginatedResponse.of(results);
    }
}
