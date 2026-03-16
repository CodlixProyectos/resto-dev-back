package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.pensioners.application.port.output.PensionerPaymentRepositoryPort;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerPayment;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity.PensionerPaymentJpaEntity;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.mapper.PensionerPaymentJpaMapper;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.repository.PensionerPaymentJpaRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PensionerPaymentRepositoryAdapter implements PensionerPaymentRepositoryPort {

    private final PensionerPaymentJpaRepository repository;
    private final PensionerPaymentJpaMapper mapper;

    @Override
    public PensionerPayment save(PensionerPayment payment) {
        PensionerPaymentJpaEntity entity = mapper.toEntity(payment);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Page<PensionerPayment> findByPensionerAndMonth(UUID pensionerId, int month, int year, Pageable pageable) {
        return repository.findByPensionerAndMonth(pensionerId, month, year, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public BigDecimal sumByPensionerAndMonth(UUID pensionerId, int month, int year) {
        return repository.sumAmountByPensionerAndMonth(pensionerId, month, year);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
