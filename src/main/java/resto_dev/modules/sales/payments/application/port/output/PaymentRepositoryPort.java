package resto_dev.modules.sales.payments.application.port.output;

import resto_dev.modules.sales.payments.domain.model.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);

    List<Payment> findByOrderId(UUID orderId);
}
