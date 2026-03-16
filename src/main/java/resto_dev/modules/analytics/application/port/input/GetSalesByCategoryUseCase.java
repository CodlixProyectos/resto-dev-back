package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.CategorySales;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetSalesByCategoryUseCase {
    List<CategorySales> execute(UUID organizationId, LocalDateTime startDate, LocalDateTime endDate);
}
