package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;

import java.util.UUID;

public interface GetOrganizationMemberUseCase {
    OrganizationMember getMember(UUID organizationId, UUID memberId);
}
