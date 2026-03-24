package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;
import resto_dev.shared.model.DateRange;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsApplicationService implements GetSalesSummaryUseCase, GetTopProductsUseCase {

    private final AnalyticsRepositoryPort repositoryPort;

    @Override
    public SalesSummary execute(UUID organizationId, DateRange dateRange) {
        if (dateRange == null) {
            dateRange = DateRange.of(
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now()
            );
        }
        return repositoryPort.getSalesSummary(organizationId, dateRange);
    }

    @Override
    public List<TopSellingProduct> execute(UUID organizationId, DateRange dateRange, int limit, int offset) {
        if (dateRange == null || dateRange.getStartDate() == null) {
            dateRange = DateRange.of(
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now()
            );
        }
        return repositoryPort.getTopSellingProducts(organizationId, dateRange, limit, offset);
    }
}
