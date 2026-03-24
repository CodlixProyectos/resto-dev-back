package resto_dev.modules.sales.payments.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.sales.payments.domain.model.Payment;
import resto_dev.modules.sales.payments.infrastructure.persistence.entity.PaymentJpaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentJpaMapper {

    public PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null)
            return null;

        return PaymentJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId() : UUID.randomUUID())
                .orderId(domain.getOrderId())
                .amount(domain.getAmount())
                .method(domain.getMethod())
                .referenceNotes(domain.getReferenceNotes())
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null)
            return null;

        return Payment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .amount(entity.getAmount())
                .method(entity.getMethod())
                .referenceNotes(entity.getReferenceNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
