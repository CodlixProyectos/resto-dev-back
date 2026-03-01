package resto_dev.modules.sales.orders.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.sales.orders.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.UUID;

@Repository
public interface OrderJpaRepository
        extends JpaRepository<OrderJpaEntity, UUID>, JpaSpecificationExecutor<OrderJpaEntity> {
}
