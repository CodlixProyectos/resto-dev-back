package resto_dev.modules.cashregister.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.cashregister.application.port.input.CloseShiftUseCase;
import resto_dev.modules.cashregister.application.port.input.GetCurrentShiftUseCase;
import resto_dev.modules.cashregister.application.port.input.OpenShiftUseCase;
import resto_dev.modules.cashregister.application.port.output.CashShiftRepositoryPort;
import resto_dev.modules.cashregister.domain.exception.ShiftAlreadyOpenException;
import resto_dev.modules.cashregister.domain.exception.ShiftNotFoundException;
import resto_dev.modules.cashregister.domain.model.CashShift;
import resto_dev.modules.cashregister.domain.model.ShiftStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CashRegisterApplicationService implements OpenShiftUseCase, CloseShiftUseCase, GetCurrentShiftUseCase {

    private final CashShiftRepositoryPort repositoryPort;

    public CashRegisterApplicationService(CashShiftRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public CashShift openShift(UUID openedBy, BigDecimal startingCash) {
        // Enforce rule: Only 1 shift open at a time
        repositoryPort.findCurrentOpenShift().ifPresent(shift -> {
            throw new ShiftAlreadyOpenException(
                    "There is already an open shift in this organization: " + shift.getId());
        });

        CashShift newShift = CashShift.builder()
                .id(UUID.randomUUID())
                .openedBy(openedBy)
                .openedAt(LocalDateTime.now())
                .startingCash(startingCash)
                .status(ShiftStatus.OPEN)
                .build();

        return repositoryPort.save(newShift);
    }

    @Override
    public CashShift closeShift(UUID shiftId, BigDecimal actualCash) {
        CashShift shift = repositoryPort.findById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException("Cash shift not found: " + shiftId));

        if (shift.getStatus() == ShiftStatus.CLOSED) {
            throw new IllegalStateException("Cash shift is already closed");
        }

        // Ideally, we would fetch total sales paid in CASH since 'openedAt' here.
        // For now, we simulate finding 0 sales to keep the architecture clean until
        // Orders module integrates.
        BigDecimal totalCashSales = BigDecimal.ZERO;

        shift.close(actualCash, totalCashSales);

        return repositoryPort.save(shift);
    }

    @Override
    public Optional<CashShift> getCurrentShift() {
        return repositoryPort.findCurrentOpenShift();
    }
}
