package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository;

import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationJpaRepository
        extends JpaRepository<OrganizationJpaEntity, UUID>, JpaSpecificationExecutor<OrganizationJpaEntity> {

    Optional<OrganizationJpaEntity> findBySlug(String slug);

    List<OrganizationJpaEntity> findByOwnerId(UUID ownerId);

    boolean existsBySlug(String slug);
}
