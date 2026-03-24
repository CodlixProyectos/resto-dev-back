package resto_dev.modules.inventory.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryCategoryJpaEntity;

import java.util.UUID;

@Repository
public interface InventoryCategoryJpaRepository extends JpaRepository<InventoryCategoryJpaEntity, UUID> {
}
