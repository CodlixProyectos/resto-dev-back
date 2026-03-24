package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.shared.model.DateRange;
import java.util.UUID;

public interface GetSalesSummaryUseCase {
    SalesSummary execute(UUID organizationId, DateRange dateRange);
}
