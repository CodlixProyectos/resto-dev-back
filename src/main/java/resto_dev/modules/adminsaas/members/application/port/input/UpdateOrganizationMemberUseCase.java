package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import java.util.UUID;

public interface UpdateOrganizationMemberUseCase {
    OrganizationMember execute(UUID organizationId, UUID memberId, UUID newRoleId, String newPin);
}
