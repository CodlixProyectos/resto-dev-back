package resto_dev.modules.adminsaas.members.application.port.input;

import java.util.UUID;

public interface DeactivateOrganizationMemberUseCase {
    void execute(UUID organizationId, UUID memberId);
}
