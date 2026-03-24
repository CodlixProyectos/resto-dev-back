package resto_dev.modules.cashregister.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.cashregister.domain.model.ShiftStatus;
import resto_dev.modules.cashregister.infrastructure.persistence.entity.CashShiftJpaEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashShiftJpaRepository extends JpaRepository<CashShiftJpaEntity, UUID> {
    Optional<CashShiftJpaEntity> findByStatus(ShiftStatus status);
}
