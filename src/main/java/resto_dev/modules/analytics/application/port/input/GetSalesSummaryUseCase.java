package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.SalesSummary;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GetSalesSummaryUseCase {
    SalesSummary execute(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate);
}
