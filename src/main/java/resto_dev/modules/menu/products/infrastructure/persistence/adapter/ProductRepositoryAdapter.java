package resto_dev.modules.menu.products.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import resto_dev.modules.menu.products.application.port.output.ProductRepositoryPort;
import resto_dev.modules.menu.products.application.query.SearchProductsQuery;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.infrastructure.persistence.entity.ProductJpaEntity;
import resto_dev.modules.menu.products.infrastructure.persistence.mapper.ProductJpaMapper;
import resto_dev.modules.menu.products.infrastructure.persistence.repository.ProductJpaRepository;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.search.GenericSpecificationBuilder;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;
    private final ProductJpaMapper mapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);
        ProductJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByNameAndCategoryId(String name, UUID categoryId) {
        return repository.existsByNameAndCategoryId(name, categoryId);
    }

    @Override
    public PageModel<Product> searchProducts(SearchProductsQuery query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());

        Specification<ProductJpaEntity> spec = Specification.where(
                GenericSpecificationBuilder.<ProductJpaEntity>searchInFields(query.getSearch(), "name", "description"))
                .and(GenericSpecificationBuilder.isEntityActive(query.getIsActive()));

        if (query.getCategoryId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("category").get("id"), query.getCategoryId()));
        }

        Page<ProductJpaEntity> page = repository.findAll(spec, pageable);

        var content = page.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());

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
