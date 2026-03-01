package resto_dev.modules.cashregister.application.port.input;

import resto_dev.modules.cashregister.domain.model.CashShift;

import java.math.BigDecimal;
import java.util.UUID;

public interface OpenShiftUseCase {
    CashShift openShift(UUID openedBy, BigDecimal startingCash);
}
