package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.application.query.GetOrganizationMembersQuery;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.shared.responses.PaginatedResponse;

/**
 * Inbound port to read and filter organization members.
 */
public interface GetOrganizationMembersUseCase {
    PaginatedResponse<OrganizationMember> getMembers(GetOrganizationMembersQuery query);
}
