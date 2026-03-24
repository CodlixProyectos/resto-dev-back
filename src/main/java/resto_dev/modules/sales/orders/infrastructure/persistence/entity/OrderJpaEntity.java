package resto_dev.modules.sales.orders.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.modules.sales.orders.domain.model.OrderType;
import resto_dev.shared.common.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@jakarta.persistence.Table(name = "restaurant_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderJpaEntity extends BaseEntity {

    @Column(nullable = true)
    private UUID tableId;

    @Column(name = "table_number", length = 50)
    private String tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private OrderType type;

    @Column(length = 150)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    private String notes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "waiter_id")
    private UUID waiterId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    public void addItems(List<OrderItemJpaEntity> newItems) {
        if (newItems != null) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            newItems.forEach(item -> {
                item.setOrder(this);
                this.items.add(item);
            });
        }
    }
}
