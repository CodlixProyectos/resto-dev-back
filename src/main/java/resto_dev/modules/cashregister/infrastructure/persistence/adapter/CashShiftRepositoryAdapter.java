package resto_dev.modules.cashregister.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import resto_dev.modules.cashregister.application.port.output.CashShiftRepositoryPort;
import resto_dev.modules.cashregister.domain.model.CashShift;
import resto_dev.modules.cashregister.domain.model.ShiftStatus;
import resto_dev.modules.cashregister.infrastructure.persistence.entity.CashShiftJpaEntity;
import resto_dev.modules.cashregister.infrastructure.persistence.mapper.CashShiftJpaMapper;
import resto_dev.modules.cashregister.infrastructure.persistence.repository.CashShiftJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Component
public class CashShiftRepositoryAdapter implements CashShiftRepositoryPort {

    private final CashShiftJpaRepository repository;
    private final CashShiftJpaMapper mapper;

    public CashShiftRepositoryAdapter(CashShiftJpaRepository repository, CashShiftJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CashShift save(CashShift cashShift) {
        CashShiftJpaEntity entity = mapper.toEntity(cashShift);
        CashShiftJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CashShift> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CashShift> findCurrentOpenShift() {
        return repository.findByStatus(ShiftStatus.OPEN)
                .map(mapper::toDomain);
    }
}
