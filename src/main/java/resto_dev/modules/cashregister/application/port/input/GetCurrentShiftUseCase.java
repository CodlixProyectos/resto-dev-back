package resto_dev.modules.cashregister.application.port.input;

import resto_dev.modules.cashregister.domain.model.CashShift;

import java.util.Optional;

public interface GetCurrentShiftUseCase {
    Optional<CashShift> getCurrentShift();
}
