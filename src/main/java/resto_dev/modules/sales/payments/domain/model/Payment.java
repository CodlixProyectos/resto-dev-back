package resto_dev.modules.sales.payments.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Payment {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String referenceNotes;
    private LocalDateTime createdAt;
}
