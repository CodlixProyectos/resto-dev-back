package resto_dev.modules.sales.payments.infrastructure.web.dto.output;

import lombok.Builder;
import lombok.Data;
import resto_dev.modules.sales.payments.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private BigDecimal amount;
    private PaymentMethod method;
    private String referenceNotes;
    private LocalDateTime createdAt;
}
