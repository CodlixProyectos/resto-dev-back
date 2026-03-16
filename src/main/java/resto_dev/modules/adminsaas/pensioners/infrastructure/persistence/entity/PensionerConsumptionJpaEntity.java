package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pensioner_consumptions", schema = "admin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PensionerConsumptionJpaEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "pensioner_id", nullable = false)
    private UUID pensionerId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "description")
    private String description;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "is_extra", nullable = false)
    private boolean isExtra;

    @Column(name = "items_snapshot", columnDefinition = "text")
    private String itemsSnapshot;

    @Column(name = "notes")
    private String notes;

    @Column(name = "payment_type", length = 50)
    private String paymentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
