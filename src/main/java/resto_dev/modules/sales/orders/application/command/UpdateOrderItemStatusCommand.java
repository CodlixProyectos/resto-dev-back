package resto_dev.modules.sales.orders.application.command;

import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;

public record UpdateOrderItemStatusCommand(
        OrderItemStatus newStatus) {
}
