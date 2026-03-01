package resto_dev.modules.sales.orders.application.command;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record CreateOrderCommand(
        UUID tableId,
        String notes,
        List<CreateOrderItemCommand> items) {
}
