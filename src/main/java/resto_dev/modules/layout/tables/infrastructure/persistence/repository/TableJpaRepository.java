package resto_dev.modules.layout.tables.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.layout.tables.infrastructure.persistence.entity.TableJpaEntity;

import java.util.UUID;

@Repository
public interface TableJpaRepository
        extends JpaRepository<TableJpaEntity, UUID>, JpaSpecificationExecutor<TableJpaEntity> {

    boolean existsByTableNumberAndZoneId(String tableNumber, UUID zoneId);
}
