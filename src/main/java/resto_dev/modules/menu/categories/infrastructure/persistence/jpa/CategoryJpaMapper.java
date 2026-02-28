package resto_dev.modules.menu.categories.infrastructure.persistence.jpa;

import org.springframework.stereotype.Component;
import resto_dev.modules.menu.categories.domain.Category;
import resto_dev.shared.common.mapper.GenericMapper;

@Component
public class CategoryJpaMapper implements GenericMapper<Category, CategoryJpaEntity> {

    @Override
    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null)
            return null;
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public CategoryJpaEntity toEntity(Category domain) {
        if (domain == null)
            return null;
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());
        // createdAt and updatedAt are managed by Hibernate
        return entity;
    }
}
