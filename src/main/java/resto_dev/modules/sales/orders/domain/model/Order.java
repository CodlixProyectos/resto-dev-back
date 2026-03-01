package resto_dev.modules.sales.orders.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Order {
    private UUID id;
    private UUID tableId;
    private OrderStatus status;
    private String notes;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void calculateTotals() {
        if (items == null || items.isEmpty()) {
            this.subtotal = BigDecimal.ZERO;
            this.tax = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
            return;
        }

        this.subtotal = items.stream()
                .map(item -> {
                    item.calculateSubtotal();
                    return item.getSubtotal();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Impuesto estándar de 18% para cálculo automático (Igv)
        this.tax = this.subtotal.multiply(new BigDecimal("0.18"));
        this.total = this.subtotal.add(this.tax);
    }
}
