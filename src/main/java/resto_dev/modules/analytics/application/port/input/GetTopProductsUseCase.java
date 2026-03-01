package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.util.List;

public interface GetTopProductsUseCase {
    List<TopSellingProduct> execute(LocalDateTime startDate, LocalDateTime endDate, int limit);
}
