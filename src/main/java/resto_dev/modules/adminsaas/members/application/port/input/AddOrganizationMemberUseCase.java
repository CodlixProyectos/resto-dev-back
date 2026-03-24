package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import java.math.BigDecimal;
import java.util.UUID;

public interface AddOrganizationMemberUseCase {
    OrganizationMember execute(UUID organizationId, String fullName, String email, String dni, String phoneNumber, String roleName, String pin, BigDecimal salary);
}

