package resto_dev.modules.layout.zones.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.layout.zones.infrastructure.persistence.entity.ZoneJpaEntity;

import java.util.UUID;

@Repository
public interface ZoneJpaRepository extends JpaRepository<ZoneJpaEntity, UUID>, JpaSpecificationExecutor<ZoneJpaEntity> {

    boolean existsByName(String name);

    long countByActiveTrue();
}
