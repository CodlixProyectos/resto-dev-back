package resto_dev.modules.adminsaas.members.application.port.output;

import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.domain.model.StaffStats;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MemberRepositoryPort {

    OrganizationMember save(OrganizationMember member);

    Optional<OrganizationMember> findById(UUID id);

    Optional<OrganizationMember> findByOrganizationAndUser(UUID organizationId, UUID userId);

    Optional<OrganizationMember> findByOrganizationAndPin(UUID organizationId, String pin);

    List<String> findPermissionsByOrganizationAndUser(UUID organizationId, UUID userId);

    StaffStats getStats(UUID organizationId);
}
