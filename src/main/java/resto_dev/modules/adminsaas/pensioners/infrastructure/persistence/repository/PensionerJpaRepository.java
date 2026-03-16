package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity.PensionerJpaEntity;

import java.util.UUID;

@Repository
public interface PensionerJpaRepository extends JpaRepository<PensionerJpaEntity, UUID> {
    Page<PensionerJpaEntity> findAllByOrganizationId(UUID organizationId, Pageable pageable);
}
