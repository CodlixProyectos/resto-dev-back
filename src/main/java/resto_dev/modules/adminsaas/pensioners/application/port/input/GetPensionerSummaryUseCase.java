package resto_dev.modules.adminsaas.pensioners.application.port.input;

import java.math.BigDecimal;
import java.util.UUID;

public interface GetPensionerSummaryUseCase {
    PensionerSummaryResponse execute(UUID pensionerId, int month, int year);

    record PensionerSummaryResponse(
            BigDecimal totalConsumed,
            BigDecimal totalPaid,
            BigDecimal pendingBalance
    ) {}
}
