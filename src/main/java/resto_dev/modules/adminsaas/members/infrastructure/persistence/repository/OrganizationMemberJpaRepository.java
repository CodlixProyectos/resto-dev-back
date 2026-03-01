package resto_dev.modules.adminsaas.members.infrastructure.persistence.repository;

import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberJpaRepository extends JpaRepository<OrganizationMemberJpaEntity, UUID>,
        JpaSpecificationExecutor<OrganizationMemberJpaEntity> {

    List<OrganizationMemberJpaEntity> findByUserId(UUID userId);

    List<OrganizationMemberJpaEntity> findByOrganizationId(UUID organizationId);

    List<OrganizationMemberJpaEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Optional<OrganizationMemberJpaEntity> findByOrganizationIdAndPin(UUID organizationId, String pin);

    boolean existsByOrganizationIdAndUserIdAndRoleId(UUID organizationId, UUID userId, UUID roleId);
}
