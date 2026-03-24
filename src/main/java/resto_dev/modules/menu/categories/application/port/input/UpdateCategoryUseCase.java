package resto_dev.modules.menu.categories.application.port.input;

import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.categories.application.command.UpdateCategoryCommand;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category execute(UUID id, UpdateCategoryCommand command);
}
