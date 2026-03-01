package resto_dev.modules.analytics.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesSummary(
                LocalDate date,
                int totalOrders,
                BigDecimal totalRevenue,
                BigDecimal averageTicket) {
}
