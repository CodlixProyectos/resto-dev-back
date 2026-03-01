package resto_dev.modules.analytics.application.port.output;

import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepositoryPort {

    SalesSummary getSalesSummary(LocalDateTime startDate, LocalDateTime endDate);

    List<TopSellingProduct> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit);
}
