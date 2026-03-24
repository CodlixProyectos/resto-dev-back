package resto_dev.modules.sales.orders.application.port.input;

import resto_dev.modules.sales.orders.domain.model.Order;
import java.util.UUID;

public interface GetOrderByIdUseCase {
    Order execute(UUID id);
}
