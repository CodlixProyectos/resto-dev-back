package resto_dev.modules.menu.products.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.infrastructure.persistence.entity.ProductJpaEntity;
import resto_dev.modules.menu.categories.infrastructure.persistence.entity.CategoryJpaEntity;

@Component
public class ProductJpaMapper {

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Product.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProductJpaEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }

        CategoryJpaEntity categoryRef = new CategoryJpaEntity();
        if (product.getCategoryId() != null) {
            categoryRef.setId(product.getCategoryId());
        }

        ProductJpaEntity entity = ProductJpaEntity.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .active(product.isActive())
                .category(product.getCategoryId() != null ? categoryRef : null)
                .build();

        if (product.getId() != null) {
            entity.setId(product.getId());
        }

        return entity;
    }
}
