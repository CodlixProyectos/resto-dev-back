package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetSalesByCategoryUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.CategorySales;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSalesByCategoryService implements GetSalesByCategoryUseCase {

    private final AnalyticsRepositoryPort repositoryPort;

    @Override
    public List<CategorySales> execute(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        return repositoryPort.getSalesByCategory(organizationId, startDate, endDate);
    }
}
