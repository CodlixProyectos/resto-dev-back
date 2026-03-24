package resto_dev.modules.sales.orders.infrastructure.web.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import resto_dev.modules.sales.orders.domain.model.OrderType;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID tableId,

        @NotNull(message = "El tipo de orden es obligatorio.")
        OrderType type,

        String customerName,

        String notes,

        @NotEmpty(message = "Debes incluir al menos un producto en la orden.") @Valid List<CreateOrderItemRequest> items) {
}
