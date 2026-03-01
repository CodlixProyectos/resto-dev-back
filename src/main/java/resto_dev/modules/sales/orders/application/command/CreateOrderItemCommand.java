package resto_dev.modules.sales.orders.application.command;

import lombok.Builder;
import java.util.UUID;

@Builder
public record CreateOrderItemCommand(
        UUID productId,
        int quantity,
        String notes) {
}
