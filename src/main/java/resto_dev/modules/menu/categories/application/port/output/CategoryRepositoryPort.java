package resto_dev.modules.menu.categories.application.port.output;

import resto_dev.modules.menu.categories.domain.model.Category;

import java.util.Optional;
import java.util.UUID;
import resto_dev.modules.menu.categories.application.query.ListCategoriesQuery;
import resto_dev.shared.common.pagination.PageModel;

/**
 * Out port for Category persistence.
 */
public interface CategoryRepositoryPort {
    Category save(Category category);

    Optional<Category> findById(UUID id);

    PageModel<Category> findAll(ListCategoriesQuery query);

    void deleteById(UUID id);
}
