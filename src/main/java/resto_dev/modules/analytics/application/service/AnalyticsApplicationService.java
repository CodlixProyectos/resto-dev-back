package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsApplicationService implements GetSalesSummaryUseCase, GetTopProductsUseCase {

    private final AnalyticsRepositoryPort analyticsRepository;

    @Override
    public SalesSummary execute(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30); // Default to last 30 days
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        return analyticsRepository.getSalesSummary(startDate, endDate);
    }

    @Override
    public List<TopSellingProduct> execute(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        if (startDate == null) {
            // Default to last 30 days if not provided
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }
        return analyticsRepository.getTopSellingProducts(startDate, endDate, limit);
    }
}
