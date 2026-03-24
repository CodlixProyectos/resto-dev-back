package resto_dev.modules.sales.payments.application.port.input;

import resto_dev.modules.sales.payments.application.command.CheckoutTableCommand;

public interface CheckoutTableUseCase {
    void execute(CheckoutTableCommand command);
}
