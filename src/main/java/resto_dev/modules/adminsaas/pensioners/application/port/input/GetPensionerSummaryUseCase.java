package resto_dev.modules.adminsaas.pensioners.application.port.input;

import java.math.BigDecimal;
import java.util.UUID;

import java.time.LocalDate;

public interface GetPensionerSummaryUseCase {
    PensionerSummaryResponse execute(UUID pensionerId, int month, int year, LocalDate startDate, LocalDate endDate);

    record PensionerSummaryResponse(
            BigDecimal totalConsumed,
            BigDecimal totalPaid,
            BigDecimal pendingBalance
    ) {}
}
