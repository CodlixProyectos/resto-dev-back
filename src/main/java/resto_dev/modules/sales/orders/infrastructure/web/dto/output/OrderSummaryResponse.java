package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        UUID tableId,
        String tableNumber,
        resto_dev.modules.sales.orders.domain.model.OrderType type,
        String customerName,
        OrderStatus status,
        String notes,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        UUID waiterId,
        String waiterName,
        LocalDateTime createdAt,
        long minutesElapsed,
        boolean overdue) {
}
