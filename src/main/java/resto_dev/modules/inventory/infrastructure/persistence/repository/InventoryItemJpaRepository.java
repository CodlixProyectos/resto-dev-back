package resto_dev.modules.inventory.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, UUID> {
    List<InventoryItemJpaEntity> findAllByActiveTrue();
    List<InventoryItemJpaEntity> findAllByCategory(String category);
}
