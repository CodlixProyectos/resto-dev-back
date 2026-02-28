package resto_dev.modules.menu.categories.ports.out;

import resto_dev.modules.menu.categories.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Out port for Category persistence.
 */
public interface CategoryRepositoryPort {
    Category save(Category category);

    Optional<Category> findById(UUID id);

    List<Category> findAll();

    void deleteById(UUID id);
}
