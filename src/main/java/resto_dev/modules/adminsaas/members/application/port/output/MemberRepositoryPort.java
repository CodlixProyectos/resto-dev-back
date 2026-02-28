package resto_dev.modules.adminsaas.members.application.port.output;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MemberRepositoryPort {

    Optional<OrganizationMember> findByOrganizationAndUser(UUID organizationId, UUID userId);

    List<String> findPermissionsByOrganizationAndUser(UUID organizationId, UUID userId);
}
