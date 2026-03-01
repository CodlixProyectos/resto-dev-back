package resto_dev.modules.menu.products.application.port.input;

import resto_dev.modules.menu.products.application.command.UpdateProductCommand;
import resto_dev.modules.menu.products.domain.model.Product;

import java.util.UUID;

public interface UpdateProductUseCase {
    Product execute(UUID id, UpdateProductCommand command);
}
