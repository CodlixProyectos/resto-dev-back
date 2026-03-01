package resto_dev.modules.adminsaas.members.application.port.output;

import resto_dev.modules.adminsaas.members.application.query.GetOrganizationMembersQuery;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import org.springframework.data.domain.Page;

/**
 * Outbound port for reading organization members (repository query side).
 */
public interface OrganizationMemberQueryPort {
    Page<OrganizationMember> searchMembers(GetOrganizationMembersQuery query);
}
