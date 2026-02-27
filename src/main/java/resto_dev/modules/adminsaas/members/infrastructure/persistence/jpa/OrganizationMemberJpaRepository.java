package resto_dev.modules.adminsaas.members.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationMemberJpaRepository extends JpaRepository<OrganizationMemberJpaEntity, UUID> {

    List<OrganizationMemberJpaEntity> findByUserId(UUID userId);

    List<OrganizationMemberJpaEntity> findByOrganizationId(UUID organizationId);

    List<OrganizationMemberJpaEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndRoleId(UUID organizationId, UUID userId, UUID roleId);
}
