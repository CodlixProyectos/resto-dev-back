package resto_dev.modules.menu.products.infrastructure.web.dto.output;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
                UUID id,
                String name,
                String description,
                BigDecimal price,
                UUID categoryId,
                String categoryName,
                boolean active,
                String imageUrl) {
}
