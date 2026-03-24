package resto_dev.modules.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemJpaEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private InventoryCategoryJpaEntity category;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "current_stock", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "min_stock", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal minStock = BigDecimal.ZERO;

    @Column(name = "max_stock", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal maxStock = BigDecimal.ZERO;

    @Column(name = "cost_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.active == null) this.active = true;
        if (this.currentStock == null) this.currentStock = BigDecimal.ZERO;
        if (this.minStock == null) this.minStock = BigDecimal.ZERO;
        if (this.maxStock == null) this.maxStock = BigDecimal.ZERO;
        if (this.costPrice == null) this.costPrice = BigDecimal.ZERO;
    }
}
