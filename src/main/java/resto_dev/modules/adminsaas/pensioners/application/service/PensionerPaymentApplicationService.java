package resto_dev.modules.adminsaas.pensioners.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import resto_dev.modules.adminsaas.pensioners.application.port.input.AddPensionerPaymentUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.DeletePensionerPaymentUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.GetPensionerPaymentsUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.GetPensionerSummaryUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.output.PensionerConsumptionRepositoryPort;
import resto_dev.modules.adminsaas.pensioners.application.port.output.PensionerPaymentRepositoryPort;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerPayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PensionerPaymentApplicationService implements AddPensionerPaymentUseCase, GetPensionerPaymentsUseCase, DeletePensionerPaymentUseCase, GetPensionerSummaryUseCase {

    private final PensionerPaymentRepositoryPort repository;
    private final PensionerConsumptionRepositoryPort consumptionRepository;

    @Override
    public PensionerPayment execute(UUID organizationId, UUID pensionerId, BigDecimal amount, LocalDate date, String method, String notes) {
        PensionerPayment payment = PensionerPayment.builder()
                .pensionerId(pensionerId)
                .organizationId(organizationId)
                .amount(amount)
                .date(date != null ? date : LocalDate.now())
                .paymentMethod(method != null ? method : "EFECTIVO")
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .build();
        return repository.save(payment);
    }

    @Override
    public Page<PensionerPayment> execute(UUID pensionerId, int month, int year, Pageable pageable) {
        return repository.findByPensionerAndMonth(pensionerId, month, year, pageable);
    }

    @Override
    public PensionerSummaryResponse execute(UUID pensionerId, int month, int year) {
        BigDecimal totalConsumed = consumptionRepository.sumByPensionerAndMonth(pensionerId, month, year);
        BigDecimal totalPaid = repository.sumByPensionerAndMonth(pensionerId, month, year);
        BigDecimal balance = totalConsumed.subtract(totalPaid);
        
        return new PensionerSummaryResponse(totalConsumed, totalPaid, balance);
    }

    @Override
    public void execute(UUID id) {
        repository.deleteById(id);
    }
}
