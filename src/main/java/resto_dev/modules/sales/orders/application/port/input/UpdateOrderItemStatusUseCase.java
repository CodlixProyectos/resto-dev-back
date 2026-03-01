package resto_dev.modules.sales.orders.application.port.input;

import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;

import java.util.UUID;

public interface UpdateOrderItemStatusUseCase {
    Order execute(UUID orderId, UUID itemId, OrderItemStatus newStatus);
}
