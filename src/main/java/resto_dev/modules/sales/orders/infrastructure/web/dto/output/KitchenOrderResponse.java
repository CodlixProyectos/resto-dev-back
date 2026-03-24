package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;

public record KitchenOrderResponse(
        UUID id,
        UUID tableId,
        String tableNumber,
        String customerName,
        OrderStatus status,
        String notes,
        List<KitchenOrderItemResponse> items,
        UUID waiterId,
        String waiterName,
        LocalDateTime createdAt,
        long minutesElapsed,
        boolean overdue) {
}
