package resto_dev.modules.menu.products.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.menu.products.application.command.CreateProductCommand;
import resto_dev.modules.menu.products.application.command.UpdateProductCommand;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.infrastructure.web.dto.input.CreateProductRequest;
import resto_dev.modules.menu.products.infrastructure.web.dto.output.ProductResponse;

@Component
public class ProductWebMapper {

    public CreateProductCommand toCommand(CreateProductRequest request) {
        return CreateProductCommand.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .categoryId(request.categoryId())
                .build();
    }

    public UpdateProductCommand toCommand(
            resto_dev.modules.menu.products.infrastructure.web.dto.input.UpdateProductRequest request) {
        return UpdateProductCommand.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .categoryId(request.categoryId())
                .active(request.active())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId(),
                product.isActive());
    }
}
