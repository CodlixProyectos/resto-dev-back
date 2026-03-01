package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import java.util.UUID;

public interface AddOrganizationMemberUseCase {
    OrganizationMember execute(UUID organizationId, String email, UUID roleId, String pin);
}
