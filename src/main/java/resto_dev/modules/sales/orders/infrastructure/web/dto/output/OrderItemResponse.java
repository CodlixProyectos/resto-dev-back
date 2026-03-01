package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String notes,
        OrderItemStatus status) {
}
