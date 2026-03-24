package resto_dev.modules.adminsaas.pensioners.application.port.input;

import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerPayment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AddPensionerPaymentUseCase {
    PensionerPayment execute(UUID organizationId, UUID pensionerId, BigDecimal amount, LocalDate date, String method, String notes);
}
