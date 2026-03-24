package resto_dev.modules.sales.orders.domain.model;

import lombok.Builder;

/**
 * Domain model for KDS dashboard statistics.
 */
@Builder
public record KitchenDashboardStats(
    long pendingCount,
    long preparingCount,
    long readyCount,
    long deliveredTodayCount
) {}
