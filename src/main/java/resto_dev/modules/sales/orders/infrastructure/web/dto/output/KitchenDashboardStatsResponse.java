package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

/**
 * API response DTO for KDS dashboard statistics.
 */
public record KitchenDashboardStatsResponse(
    long pendingCount,
    long preparingCount,
    long readyCount,
    long deliveredTodayCount
) {}
