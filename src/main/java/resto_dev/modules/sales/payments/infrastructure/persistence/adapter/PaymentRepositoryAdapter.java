package resto_dev.modules.sales.payments.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.sales.payments.application.port.output.PaymentRepositoryPort;
import resto_dev.modules.sales.payments.domain.model.Payment;
import resto_dev.modules.sales.payments.infrastructure.persistence.entity.PaymentJpaEntity;
import resto_dev.modules.sales.payments.infrastructure.persistence.mapper.PaymentJpaMapper;
import resto_dev.modules.sales.payments.infrastructure.persistence.repository.PaymentJpaRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentJpaMapper mapper;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toEntity(payment);
        PaymentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
