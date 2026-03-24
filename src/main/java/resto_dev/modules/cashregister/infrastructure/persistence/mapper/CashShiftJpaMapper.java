package resto_dev.modules.cashregister.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.cashregister.domain.model.CashShift;
import resto_dev.modules.cashregister.infrastructure.persistence.entity.CashShiftJpaEntity;
import resto_dev.shared.common.mapper.GenericMapper;

@Component
public class CashShiftJpaMapper implements GenericMapper<CashShift, CashShiftJpaEntity> {

    @Override
    public CashShift toDomain(CashShiftJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return CashShift.builder()
                .id(entity.getId())
                .openedBy(entity.getOpenedBy())
                .openedAt(entity.getOpenedAt())
                .closedAt(entity.getClosedAt())
                .startingCash(entity.getStartingCash())
                .expectedCash(entity.getExpectedCash())
                .actualCash(entity.getActualCash())
                .difference(entity.getDifference())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public CashShiftJpaEntity toEntity(CashShift domain) {
        if (domain == null) {
            return null;
        }

        return CashShiftJpaEntity.builder()
                .id(domain.getId())
                .openedBy(domain.getOpenedBy())
                .openedAt(domain.getOpenedAt())
                .closedAt(domain.getClosedAt())
                .startingCash(domain.getStartingCash())
                .expectedCash(domain.getExpectedCash())
                .actualCash(domain.getActualCash())
                .difference(domain.getDifference())
                .status(domain.getStatus())
                .build();
    }
}
