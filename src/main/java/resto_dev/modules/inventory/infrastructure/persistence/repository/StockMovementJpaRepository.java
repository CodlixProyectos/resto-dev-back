package resto_dev.modules.inventory.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.inventory.infrastructure.persistence.entity.StockMovementJpaEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementJpaEntity, UUID> {
    List<StockMovementJpaEntity> findAllByItemIdOrderByDateDesc(UUID itemId);
}
