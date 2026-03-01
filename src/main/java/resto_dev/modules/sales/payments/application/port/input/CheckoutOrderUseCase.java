package resto_dev.modules.sales.payments.application.port.input;

import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.payments.application.command.CheckoutOrderCommand;

public interface CheckoutOrderUseCase {
    Order execute(CheckoutOrderCommand command);
}
