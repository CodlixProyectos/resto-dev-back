package resto_dev.modules.sales.orders.application.port.input;

import resto_dev.modules.sales.orders.application.command.CreateOrderCommand;
import resto_dev.modules.sales.orders.domain.model.Order;

public interface CreateOrderUseCase {
    Order execute(CreateOrderCommand command);
}
