package resto_dev.modules.sales.orders.application.query;

import lombok.Builder;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

@Builder
public record SearchOrdersQuery(
        UUID tableId,
        UUID waiterId,
        List<OrderStatus> statuses, 
        java.time.LocalDateTime startDate,
        java.time.LocalDateTime endDate,
        String searchTerm,
        int page,
        int size) {
}
