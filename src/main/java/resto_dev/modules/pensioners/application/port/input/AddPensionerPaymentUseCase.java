package resto_dev.modules.pensioners.application.port.input;

import resto_dev.modules.pensioners.domain.model.PensionerPayment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AddPensionerPaymentUseCase {
    PensionerPayment execute(UUID organizationId, UUID pensionerId, BigDecimal amount, LocalDate date, String method, String notes);
}
