package resto_dev.modules.menu.products.application.command;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CreateProductCommand(
        String name,
        String description,
        BigDecimal price,
        UUID categoryId) {
}
