package resto_dev.modules.adminsaas.pensioners.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import resto_dev.modules.adminsaas.pensioners.application.port.input.AddConsumptionUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.DeleteConsumptionUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.GetConsumptionsUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.output.PensionerConsumptionRepositoryPort;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerConsumption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PensionerConsumptionApplicationService
        implements AddConsumptionUseCase, GetConsumptionsUseCase, DeleteConsumptionUseCase {

    private final PensionerConsumptionRepositoryPort repository;

    @Override
    public PensionerConsumption execute(UUID organizationId, UUID pensionerId, LocalDate date,
                                        String description, BigDecimal totalAmount, boolean isExtra,
                                        String itemsSnapshot, String notes, String paymentType) {
        PensionerConsumption consumption = PensionerConsumption.builder()
                .pensionerId(pensionerId)
                .organizationId(organizationId)
                .date(date != null ? date : LocalDate.now())
                .description(description)
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .isExtra(isExtra)
                .itemsSnapshot(itemsSnapshot)
                .notes(notes)
                .paymentType(paymentType != null ? paymentType : "EFECTIVO")
                .build();

        return repository.save(consumption);
    }

    @Override
    public Page<PensionerConsumption> execute(UUID pensionerId, int month, int year, Pageable pageable) {
        return repository.findByPensionerAndMonth(pensionerId, month, year, pageable);
    }

    @Override
    public void execute(UUID consumptionId) {
        repository.deleteById(consumptionId);
    }
}
