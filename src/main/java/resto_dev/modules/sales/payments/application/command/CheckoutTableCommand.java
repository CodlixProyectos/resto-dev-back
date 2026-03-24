package resto_dev.modules.sales.payments.application.command;

import java.util.List;
import java.util.UUID;

public record CheckoutTableCommand(
        UUID tableId,
        List<CheckoutOrderCommand.PaymentSplitCommand> splits,
        String cashierNotes) {
}
