package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.application.port.input.GetRevenueHistoryUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;
import resto_dev.modules.analytics.domain.model.DailyRevenue;
import resto_dev.shared.model.DateRange;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import resto_dev.modules.analytics.application.port.input.GetInventoryAnalyticsUseCase;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsApplicationService implements GetSalesSummaryUseCase, GetTopProductsUseCase, GetRevenueHistoryUseCase, GetInventoryAnalyticsUseCase {

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

    @Override
    public List<DailyRevenue> getRevenueHistory(UUID organizationId, DateRange dateRange) {
        if (dateRange == null || dateRange.getStartDate() == null) {
            dateRange = DateRange.of(
                LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now()
            );
        }

        List<DailyRevenue> existingRevenue = repositoryPort.getRevenueHistory(organizationId, dateRange);

        // Fill gaps with $0 revenue for a continuous timeline
        java.time.LocalDate start = dateRange.getStartDate().toLocalDate();
        java.time.LocalDate end = dateRange.getEndDate().toLocalDate();

        java.util.Map<java.time.LocalDate, java.math.BigDecimal> revenueMap = existingRevenue.stream()
            .collect(java.util.stream.Collectors.toMap(
                DailyRevenue::getDate, 
                DailyRevenue::getRevenue,
                (v1, v2) -> v1 // In case of duplicates, though not expected
            ));

        java.util.List<DailyRevenue> fullHistory = new java.util.ArrayList<>();
        java.time.LocalDate current = start;

        // Ensure we don't go beyond today if the range ends in the future
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate actualEnd = end.isAfter(today) ? today : end;

        while (!current.isAfter(actualEnd)) {
            java.math.BigDecimal revenue = revenueMap.getOrDefault(current, java.math.BigDecimal.ZERO);
            fullHistory.add(new DailyRevenue(current, revenue));
            current = current.plusDays(1);
        }

        return fullHistory;
    }

    public resto_dev.modules.analytics.domain.model.InventoryAnalytics getInventoryAnalytics(UUID organizationId) {
        return repositoryPort.getInventoryAnalytics(organizationId);
    }
}
