package resto_dev.modules.sales.payments.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.sales.payments.infrastructure.persistence.entity.PaymentJpaEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    List<PaymentJpaEntity> findByOrderId(UUID orderId);
}
