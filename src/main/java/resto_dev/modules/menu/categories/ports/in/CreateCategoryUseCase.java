package resto_dev.modules.menu.categories.ports.in;

import resto_dev.modules.menu.categories.domain.Category;
import resto_dev.modules.menu.categories.ports.in.dto.CreateCategoryCommand;

/**
 * Input port for creating a category.
 */
public interface CreateCategoryUseCase {
    Category execute(CreateCategoryCommand command);
}
