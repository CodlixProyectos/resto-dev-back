package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetSalesByCategoryUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.shared.model.DateRange;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSalesByCategoryService implements GetSalesByCategoryUseCase {

    private final AnalyticsRepositoryPort repositoryPort;

    @Override
    public List<CategorySales> execute(UUID organizationId, DateRange dateRange) {
        if (dateRange == null || dateRange.getStartDate() == null) {
            dateRange = DateRange.of(
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now()
            );
        }
        return repositoryPort.getSalesByCategory(organizationId, dateRange);
    }
}
