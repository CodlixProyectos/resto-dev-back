package resto_dev.modules.sales.orders.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;
import resto_dev.shared.common.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@jakarta.persistence.Table(name = "restaurant_order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemJpaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderItemStatus status;
}
