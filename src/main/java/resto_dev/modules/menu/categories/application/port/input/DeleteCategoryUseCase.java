package resto_dev.modules.menu.categories.application.port.input;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void execute(UUID id);
}
