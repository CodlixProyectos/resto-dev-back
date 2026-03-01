package resto_dev.modules.sales.payments.application.command;

import resto_dev.modules.sales.payments.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutOrderCommand(
        UUID orderId,
        java.util.List<PaymentSplitCommand> splits,
        String cashierNotes) {
    public record PaymentSplitCommand(
            BigDecimal amount,
            PaymentMethod method,
            String referenceNotes) {
    }
}
