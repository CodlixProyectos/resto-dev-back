package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateOrganizationMemberUseCase {
    OrganizationMember execute(UUID organizationId, UUID memberId, UUID newRoleId, String currentPin, String newPin, BigDecimal newSalary, String newStatus, String newRoleName);
}
