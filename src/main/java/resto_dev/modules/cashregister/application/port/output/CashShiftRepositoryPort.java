package resto_dev.modules.cashregister.application.port.output;

import resto_dev.modules.cashregister.domain.model.CashShift;

import java.util.Optional;
import java.util.UUID;

public interface CashShiftRepositoryPort {
    CashShift save(CashShift cashShift);

    Optional<CashShift> findById(UUID id);

    Optional<CashShift> findCurrentOpenShift();
}
