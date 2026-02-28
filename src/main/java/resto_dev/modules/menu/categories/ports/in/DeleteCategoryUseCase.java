package resto_dev.modules.menu.categories.ports.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void execute(UUID id);
}
