package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.DailyRevenue;
import resto_dev.shared.model.DateRange;

import java.util.List;
import java.util.UUID;

public interface GetRevenueHistoryUseCase {
    List<DailyRevenue> getRevenueHistory(UUID organizationId, DateRange dateRange);
}
