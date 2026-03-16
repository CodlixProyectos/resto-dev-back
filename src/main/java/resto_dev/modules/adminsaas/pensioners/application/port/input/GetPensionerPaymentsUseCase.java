package resto_dev.modules.adminsaas.pensioners.application.port.input;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerPayment;

import java.util.UUID;

public interface GetPensionerPaymentsUseCase {
    Page<PensionerPayment> execute(UUID pensionerId, int month, int year, Pageable pageable);
}
