package resto_dev.modules.inventory.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, UUID>, JpaSpecificationExecutor<InventoryItemJpaEntity> {
    List<InventoryItemJpaEntity> findAllByActiveTrue();
    Page<InventoryItemJpaEntity> findAllByActiveTrue(Pageable pageable);
    Page<InventoryItemJpaEntity> findAllByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<InventoryItemJpaEntity> findAllByCategory(String category);
    boolean existsByNameIgnoreCaseAndActiveTrue(String name);
    java.util.Optional<InventoryItemJpaEntity> findByNameIgnoreCaseAndActiveTrue(String name);
}
