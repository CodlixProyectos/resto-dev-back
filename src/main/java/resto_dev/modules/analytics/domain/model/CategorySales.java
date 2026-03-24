package resto_dev.modules.analytics.domain.model;

import java.math.BigDecimal;

public record CategorySales(
    String categoryName,
    long totalOrders,
    BigDecimal totalRevenue,
    double percentage
) {}
