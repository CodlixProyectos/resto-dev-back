package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.pensioners.application.port.output.PensionerConsumptionRepositoryPort;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerConsumption;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.mapper.PensionerConsumptionJpaMapper;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.repository.PensionerConsumptionJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PensionerConsumptionRepositoryAdapter implements PensionerConsumptionRepositoryPort {

    private final PensionerConsumptionJpaRepository jpaRepository;
    private final PensionerConsumptionJpaMapper mapper;

    @Override
    public PensionerConsumption save(PensionerConsumption consumption) {
        if (consumption.getId() == null) {
            consumption.setCreatedAt(LocalDateTime.now());
        }
        var entity = mapper.toEntity(consumption);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Page<PensionerConsumption> findByPensionerAndMonth(UUID pensionerId, int month, int year, Pageable pageable) {
        return jpaRepository.findByPensionerAndMonth(pensionerId, month, year, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public BigDecimal sumByPensionerAndMonth(UUID pensionerId, int month, int year) {
        return jpaRepository.sumTotalAmountByPensionerAndMonth(pensionerId, month, year);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
