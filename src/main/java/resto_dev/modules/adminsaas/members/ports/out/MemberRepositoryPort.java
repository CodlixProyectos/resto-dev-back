package resto_dev.modules.adminsaas.members.ports.out;

import resto_dev.modules.adminsaas.members.domain.OrganizationMember;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MemberRepositoryPort {

    Optional<OrganizationMember> findByOrganizationAndUser(UUID organizationId, UUID userId);

    List<String> findPermissionsByOrganizationAndUser(UUID organizationId, UUID userId);
}
