package resto_dev.modules.adminsaas.pensioners.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PensionerPayment {
    private UUID id;
    private UUID pensionerId;
    private UUID organizationId;
    private BigDecimal amount;
    private LocalDate date;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
}
