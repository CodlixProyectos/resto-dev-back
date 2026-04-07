package resto_dev.modules.pensioners.application.port.input;

import resto_dev.modules.pensioners.domain.model.PensionerConsumption;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AddConsumptionUseCase {
    PensionerConsumption execute(UUID organizationId, UUID pensionerId, LocalDate date,
                                 String description, BigDecimal totalAmount, boolean isExtra,
                                 String itemsSnapshot, String notes, String paymentType);
}
