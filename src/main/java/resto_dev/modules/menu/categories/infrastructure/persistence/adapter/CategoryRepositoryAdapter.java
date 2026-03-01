package resto_dev.modules.menu.categories.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.categories.infrastructure.persistence.entity.CategoryJpaEntity;
import resto_dev.modules.menu.categories.infrastructure.persistence.mapper.CategoryJpaMapper;
import resto_dev.modules.menu.categories.infrastructure.persistence.repository.CategoryJpaRepository;
import resto_dev.modules.menu.categories.application.port.output.CategoryRepositoryPort;

import resto_dev.modules.menu.categories.application.query.ListCategoriesQuery;
import resto_dev.modules.menu.categories.infrastructure.persistence.repository.CategorySpecification;
import resto_dev.shared.common.pagination.PageModel;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository repository;
    private final CategoryJpaMapper mapper;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = mapper.toEntity(category);
        CategoryJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageModel<Category> findAll(ListCategoriesQuery query) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(query.getPage(), query.getSize());
        org.springframework.data.jpa.domain.Specification<CategoryJpaEntity> spec = CategorySpecification
                .withFilters(query.getName(), query.getIsActive());

        org.springframework.data.domain.Page<CategoryJpaEntity> page = repository.findAll(spec, pageable);

        var content = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageModel<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
