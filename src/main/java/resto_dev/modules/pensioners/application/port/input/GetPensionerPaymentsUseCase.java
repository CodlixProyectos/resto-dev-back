package resto_dev.modules.pensioners.application.port.input;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.pensioners.domain.model.PensionerPayment;

import java.util.UUID;

import java.time.LocalDate;

public interface GetPensionerPaymentsUseCase {
    Page<PensionerPayment> execute(UUID pensionerId, int month, int year, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
