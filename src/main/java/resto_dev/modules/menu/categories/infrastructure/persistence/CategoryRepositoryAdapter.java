package resto_dev.modules.menu.categories.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.menu.categories.domain.Category;
import resto_dev.modules.menu.categories.infrastructure.persistence.jpa.CategoryJpaEntity;
import resto_dev.modules.menu.categories.infrastructure.persistence.jpa.CategoryJpaMapper;
import resto_dev.modules.menu.categories.infrastructure.persistence.jpa.CategoryJpaRepository;
import resto_dev.modules.menu.categories.ports.out.CategoryRepositoryPort;

import java.util.List;
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
    public List<Category> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
