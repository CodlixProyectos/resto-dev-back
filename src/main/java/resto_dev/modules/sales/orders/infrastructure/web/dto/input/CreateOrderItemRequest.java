package resto_dev.modules.sales.orders.infrastructure.web.dto.input;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrderItemRequest(
        @NotNull(message = "El ID del producto es obligatorio.") UUID productId,

        @Min(value = 1, message = "La cantidad debe ser mayor a 0.") int quantity,

        String notes) {
}
