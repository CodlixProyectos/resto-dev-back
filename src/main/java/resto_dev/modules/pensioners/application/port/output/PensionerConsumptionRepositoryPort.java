package resto_dev.modules.pensioners.application.port.output;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.pensioners.domain.model.PensionerConsumption;

import java.math.BigDecimal;
import java.util.UUID;

import java.time.LocalDate;

public interface PensionerConsumptionRepositoryPort {
    PensionerConsumption save(PensionerConsumption consumption);
    Page<PensionerConsumption> findByPensionerAndMonth(UUID pensionerId, int month, int year, Pageable pageable);
    Page<PensionerConsumption> findByPensionerAndDateRange(UUID pensionerId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    BigDecimal sumByPensionerAndMonth(UUID pensionerId, int month, int year);
    BigDecimal sumByPensionerAndDateRange(UUID pensionerId, LocalDate startDate, LocalDate endDate);
    void deleteById(UUID id);
}
