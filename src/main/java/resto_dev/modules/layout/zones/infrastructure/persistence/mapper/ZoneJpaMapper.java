package resto_dev.modules.layout.zones.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.modules.layout.zones.infrastructure.persistence.entity.ZoneJpaEntity;

@Component
public class ZoneJpaMapper {

    public Zone toDomain(ZoneJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Zone.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.isActive())
                .entrancePosX(entity.getEntrancePosX())
                .entrancePosY(entity.getEntrancePosY())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ZoneJpaEntity toEntity(Zone zone) {
        if (zone == null) {
            return null;
        }

        ZoneJpaEntity entity = ZoneJpaEntity.builder()
                .name(zone.getName())
                .description(zone.getDescription())
                .active(zone.isActive())
                .entrancePosX(zone.getEntrancePosX())
                .entrancePosY(zone.getEntrancePosY())
                .build();

        if (zone.getId() != null) {
            entity.setId(zone.getId());
        }

        return entity;
    }
}
