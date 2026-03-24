package resto_dev.modules.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementJpaEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "type", nullable = false, length = 30)
    private String type; // COMPRA, CONSUMO, AJUSTE, MERMA

    @Column(name = "quantity", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
