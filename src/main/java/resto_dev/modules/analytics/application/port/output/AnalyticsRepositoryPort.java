package resto_dev.modules.analytics.application.port.output;

import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AnalyticsRepositoryPort {

    SalesSummary getSalesSummary(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate);

    List<TopSellingProduct> getTopSellingProducts(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate, int limit);

    List<RecentActivity> getRecentActivity(UUID organizationId, int limit);

    List<CategorySales> getSalesByCategory(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate);
}
