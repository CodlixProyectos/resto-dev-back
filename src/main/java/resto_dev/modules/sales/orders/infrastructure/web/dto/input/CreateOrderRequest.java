package resto_dev.modules.sales.orders.infrastructure.web.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "El ID de la mesa es obligatorio.") UUID tableId,

        String notes,

        @NotEmpty(message = "Debes incluir al menos un producto en la orden.") @Valid List<CreateOrderItemRequest> items) {
}
