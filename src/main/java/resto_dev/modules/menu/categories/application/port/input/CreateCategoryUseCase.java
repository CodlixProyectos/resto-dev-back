package resto_dev.modules.menu.categories.application.port.input;

import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.categories.application.command.CreateCategoryCommand;

/**
 * Input port for creating a category.
 */
public interface CreateCategoryUseCase {
    Category execute(CreateCategoryCommand command);
}
