package resto_dev.modules.analytics.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TopSellingProduct(
        UUID productId,
        String productName,
        int quantitySold,
        BigDecimal totalRevenue) {
}
