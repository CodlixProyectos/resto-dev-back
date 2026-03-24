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
public class PensionerConsumption {
    private UUID id;
    private UUID pensionerId;
    private UUID organizationId;
    private LocalDate date;
    private String description;
    private BigDecimal totalAmount;
    private boolean isExtra;
    private String itemsSnapshot; // JSON con los productos
    private String notes;
    private String paymentType;
    private LocalDateTime createdAt;
}
