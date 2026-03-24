package resto_dev.modules.sales.orders.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotNull;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;

public record UpdateOrderItemStatusRequest(
        @NotNull(message = "El nuevo estado del ítem es obligatorio.") OrderItemStatus newStatus) {
}
