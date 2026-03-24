package resto_dev.modules.menu.products.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank(message = "Product name is required") String name,

        String description,

        @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal price,

        @NotNull(message = "Category ID is required") UUID categoryId,

        boolean active,
        
        String imageUrl) {
}
