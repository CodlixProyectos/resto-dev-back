package resto_dev.modules.sales.orders.application.command;

import lombok.Builder;
import resto_dev.modules.sales.orders.domain.model.OrderType;
import java.util.List;
import java.util.UUID;

@Builder
public record CreateOrderCommand(
        UUID tableId,
        OrderType type,
        String customerName,
        String notes,
        UUID waiterId,
        List<CreateOrderItemCommand> items) {
}
