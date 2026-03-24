package resto_dev.modules.sales.orders.application.port.input;

import resto_dev.modules.sales.orders.domain.model.KitchenDashboardStats;

/**
 * Use case to retrieve statistics for the KDS dashboard.
 */
public interface GetKitchenDashboardStatsUseCase {
    KitchenDashboardStats execute();
}
