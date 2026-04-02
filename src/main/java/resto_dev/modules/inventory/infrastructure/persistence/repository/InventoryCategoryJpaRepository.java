package resto_dev.modules.inventory.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryCategoryJpaEntity;

import java.util.UUID;

@Repository
public interface InventoryCategoryJpaRepository extends JpaRepository<InventoryCategoryJpaEntity, UUID> {
    Page<InventoryCategoryJpaEntity> findAll(Pageable pageable);
    Page<InventoryCategoryJpaEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<InventoryCategoryJpaEntity> findAllByActiveTrue(Pageable pageable);
}
