package resto_dev.modules.sales.orders.application.query;

import lombok.Builder;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

@Builder
public record SearchOrdersQuery(
        UUID tableId,
        List<OrderStatus> statuses, // Para el KDS mandaremos [PENDING_KITCHEN, PREPARING]
        int page,
        int size) {
}
