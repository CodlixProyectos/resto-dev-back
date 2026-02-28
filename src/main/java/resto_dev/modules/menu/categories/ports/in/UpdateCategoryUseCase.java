package resto_dev.modules.menu.categories.ports.in;

import resto_dev.modules.menu.categories.domain.Category;
import resto_dev.modules.menu.categories.ports.in.dto.UpdateCategoryCommand;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category execute(UUID id, UpdateCategoryCommand command);
}
