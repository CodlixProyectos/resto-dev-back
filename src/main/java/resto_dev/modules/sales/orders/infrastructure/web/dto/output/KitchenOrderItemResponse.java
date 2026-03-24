package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

import java.util.UUID;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;

public record KitchenOrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        int quantity,
        String notes,
        OrderItemStatus status) {
}
