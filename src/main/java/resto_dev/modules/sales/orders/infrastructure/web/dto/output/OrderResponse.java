package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID tableId,
        OrderStatus status,
        String notes,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        List<OrderItemResponse> items,
        LocalDateTime createdAt) {
}
