package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.TopSellingProduct;
import resto_dev.shared.model.DateRange;
import java.util.List;
import java.util.UUID;

public interface GetTopProductsUseCase {
    List<TopSellingProduct> execute(UUID organizationId, DateRange dateRange, int limit, int offset);
}
