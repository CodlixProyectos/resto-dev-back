package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.InventoryAnalytics;
import java.util.UUID;

public interface GetInventoryAnalyticsUseCase {
    InventoryAnalytics getInventoryAnalytics(UUID organizationId);
}
