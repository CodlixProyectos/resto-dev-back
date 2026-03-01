package resto_dev.modules.sales.payments.infrastructure.web.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import resto_dev.modules.sales.payments.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutRequest {

    private String cashierNotes;

    @NotEmpty(message = "Debe haber al menos un método de pago")
    private List<PaymentSplitRequest> splits;

    @Data
    public static class PaymentSplitRequest {
        @NotNull(message = "El monto no puede ser nulo")
        @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a 0")
        private BigDecimal amount;

        @NotNull(message = "El método de pago es obligatorio")
        private PaymentMethod method;

        private String referenceNotes;
    }
}
