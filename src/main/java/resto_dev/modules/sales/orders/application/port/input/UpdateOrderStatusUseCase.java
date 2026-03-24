package resto_dev.modules.sales.orders.application.port.input;

import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;

import java.util.UUID;

public interface UpdateOrderStatusUseCase {
    Order execute(UUID orderId, OrderStatus newStatus);
}
