package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.shared.model.DateRange;
import java.util.List;
import java.util.UUID;

public interface GetSalesByCategoryUseCase {
    List<CategorySales> execute(UUID organizationId, DateRange dateRange);
}
