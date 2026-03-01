package resto_dev.modules.sales.orders.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderItem {
    private UUID id;
    private UUID orderId;
    private UUID productId;

    // Snapshot del nombre y precio en el momento de crear la orden
    // por si el producto físico cambia o se elimina después
    private String productName;
    private BigDecimal unitPrice;

    private int quantity;
    private BigDecimal subtotal;
    private String notes;
    private OrderItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void calculateSubtotal() {
        if (unitPrice != null && quantity > 0) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
