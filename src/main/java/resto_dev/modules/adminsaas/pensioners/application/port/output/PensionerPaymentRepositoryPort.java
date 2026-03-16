package resto_dev.modules.adminsaas.pensioners.application.port.output;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerPayment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PensionerPaymentRepositoryPort {
    PensionerPayment save(PensionerPayment payment);
    Page<PensionerPayment> findByPensionerAndMonth(UUID pensionerId, int month, int year, Pageable pageable);
    BigDecimal sumByPensionerAndMonth(UUID pensionerId, int month, int year);
    void deleteById(UUID id);
}
