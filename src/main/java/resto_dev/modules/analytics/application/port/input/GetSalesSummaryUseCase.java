package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.SalesSummary;

import java.time.LocalDateTime;

public interface GetSalesSummaryUseCase {
    SalesSummary execute(LocalDateTime startDate, LocalDateTime endDate);
}
