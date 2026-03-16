package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetRecentActivityUseCase;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsApplicationService implements GetSalesSummaryUseCase, GetTopProductsUseCase, GetRecentActivityUseCase {

    private final AnalyticsRepositoryPort repositoryPort;

    @Override
    public SalesSummary execute(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        return repositoryPort.getSalesSummary(organizationId, startDate, endDate);
    }

    @Override
    public List<TopSellingProduct> execute(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate, int limit) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        // Limit validation is now handled by the repository or use case input validation
        return repositoryPort.getTopSellingProducts(organizationId, startDate, endDate, limit);
    }

    @Override
    public List<RecentActivity> execute(UUID organizationId, int limit) {
        // Limit validation is now handled by the repository or use case input validation
        return repositoryPort.getRecentActivity(organizationId, limit);
    }
}
