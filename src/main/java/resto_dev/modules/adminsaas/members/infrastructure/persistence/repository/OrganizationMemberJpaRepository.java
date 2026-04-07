package resto_dev.modules.adminsaas.members.infrastructure.persistence.repository;

import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberJpaRepository extends JpaRepository<OrganizationMemberJpaEntity, UUID>,
        JpaSpecificationExecutor<OrganizationMemberJpaEntity> {

    List<OrganizationMemberJpaEntity> findByUserId(UUID userId);

    @Query("SELECT m FROM OrganizationMemberJpaEntity m JOIN FETCH m.organization WHERE m.user.id = :userId AND m.active = true")
    List<OrganizationMemberJpaEntity> findActiveByUserIdWithOrganization(@Param("userId") UUID userId);



    List<OrganizationMemberJpaEntity> findByOrganizationId(UUID organizationId);

    List<OrganizationMemberJpaEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Optional<OrganizationMemberJpaEntity> findByOrganizationIdAndPin(UUID organizationId, String pin);

    @Query("SELECT m FROM OrganizationMemberJpaEntity m JOIN m.user u WHERE u.email = :email AND m.pin = :pin AND m.active = true")
    Optional<OrganizationMemberJpaEntity> findByUserEmailAndPin(@Param("email") String email, @Param("pin") String pin);

    @Query("SELECT m FROM OrganizationMemberJpaEntity m JOIN m.user u WHERE u.dni = :dni AND m.pin = :pin AND m.active = true")
    Optional<OrganizationMemberJpaEntity> findByUserDniAndPin(@Param("dni") String dni, @Param("pin") String pin);

    boolean existsByOrganizationIdAndUserIdAndRoleId(UUID organizationId, UUID userId, UUID roleId);

    long countByOrganizationId(UUID organizationId);

    long countByOrganizationIdAndStatus(UUID organizationId, String status);

    @Query("SELECT SUM(m.salary) FROM OrganizationMemberJpaEntity m WHERE m.organization.id = :organizationId")
    BigDecimal sumSalaryByOrganizationId(@Param("organizationId") UUID organizationId);
}
