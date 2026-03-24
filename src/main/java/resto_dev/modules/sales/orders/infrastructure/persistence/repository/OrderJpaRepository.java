package resto_dev.modules.sales.orders.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.sales.orders.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.UUID;

@Repository
public interface OrderJpaRepository
        extends JpaRepository<OrderJpaEntity, UUID>, JpaSpecificationExecutor<OrderJpaEntity> {

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM OrderJpaEntity o WHERE o.waiterId = :waiterId AND CAST(o.createdAt AS date) = :date")
    long countByWaiterIdAndDate(@org.springframework.data.repository.query.Param("waiterId") UUID waiterId, @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);

    long countByStatus(resto_dev.modules.sales.orders.domain.model.OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM OrderJpaEntity o WHERE o.status = :status AND CAST(o.createdAt AS date) = :date")
    long countByStatusAndDate(@org.springframework.data.repository.query.Param("status") resto_dev.modules.sales.orders.domain.model.OrderStatus status, @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);
}
