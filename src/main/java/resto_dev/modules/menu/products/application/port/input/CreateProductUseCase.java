package resto_dev.modules.menu.products.application.port.input;

import resto_dev.modules.menu.products.application.command.CreateProductCommand;
import resto_dev.modules.menu.products.domain.model.Product;

public interface CreateProductUseCase {
    Product execute(CreateProductCommand command);
}
