package resto_dev.modules.analytics.application.port.output;

import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;
import resto_dev.shared.model.DateRange;

import java.util.List;
import java.util.UUID;

public interface AnalyticsRepositoryPort {

    SalesSummary getSalesSummary(UUID organizationId, DateRange dateRange);

    List<TopSellingProduct> getTopSellingProducts(UUID organizationId, DateRange dateRange, int limit, int offset);

    List<RecentActivity> getRecentActivity(UUID organizationId, DateRange dateRange, int limit, int offset);

    List<CategorySales> getSalesByCategory(UUID organizationId, DateRange dateRange);

    List<resto_dev.modules.analytics.domain.model.DailyRevenue> getRevenueHistory(UUID organizationId, DateRange dateRange);

    resto_dev.modules.analytics.domain.model.InventoryAnalytics getInventoryAnalytics(UUID organizationId);
}
