package resto_dev.modules.cashregister.application.port.input;

import resto_dev.modules.cashregister.domain.model.CashShift;

import java.math.BigDecimal;
import java.util.UUID;

public interface CloseShiftUseCase {
    CashShift closeShift(UUID shiftId, BigDecimal actualCash);
}
