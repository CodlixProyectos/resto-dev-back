package resto_dev.modules.sales.orders.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotNull;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull(message = "El nuevo estado de la orden es obligatorio.") OrderStatus newStatus) {
}
