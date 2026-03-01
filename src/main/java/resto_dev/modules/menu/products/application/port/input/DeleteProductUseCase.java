package resto_dev.modules.menu.products.application.port.input;

import java.util.UUID;

public interface DeleteProductUseCase {
    void execute(UUID id);
}
