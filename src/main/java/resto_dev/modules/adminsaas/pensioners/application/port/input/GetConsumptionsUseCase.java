package resto_dev.modules.adminsaas.pensioners.application.port.input;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerConsumption;
import java.util.UUID;

import java.time.LocalDate;

public interface GetConsumptionsUseCase {
    Page<PensionerConsumption> execute(UUID pensionerId, int month, int year, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
